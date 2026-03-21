package com.starclient.render;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.DepthTestFunction;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.starclient.StarClientOptions;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.world.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MappableRingBuffer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryUtil;

import java.awt.Color;
import java.nio.ByteBuffer;
import java.util.Objects;
import java.util.OptionalDouble;
import java.util.OptionalInt;

public final class MobChamsRenderer {
    private static final @NonNull MobChamsRenderer INSTANCE = new MobChamsRenderer();

    private static final @NonNull RenderPipeline FILLED_THROUGH_WALLS = RenderPipelines.register(
            RenderPipeline.builder(RenderPipelines.DEBUG_FILLED_SNIPPET)
                    .withLocation(Identifier.fromNamespaceAndPath("starclient", "pipeline/mob_chams"))
                    .withDepthTestFunction(DepthTestFunction.NO_DEPTH_TEST)
                    .build());

    private static final @NonNull ByteBufferBuilder ALLOCATOR = new ByteBufferBuilder(RenderType.SMALL_BUFFER_SIZE);
    private static final @NonNull Vector4f COLOR_MODULATOR = new Vector4f(1f, 1f, 1f, 1f);
    private static final @NonNull Vector3f MODEL_OFFSET = new Vector3f();
    private static final @NonNull Matrix4f TEXTURE_MATRIX = new Matrix4f();
    private static final OptionalInt NO_STENCIL = OptionalInt.empty();
    private static final OptionalDouble NO_CLEAR_DEPTH = OptionalDouble.empty();

    private @Nullable BufferBuilder buffer;
    private @Nullable MappableRingBuffer vertexBuffer;
    private boolean hasPendingGeometry;

    private MobChamsRenderer() {
    }

    public static @NonNull MobChamsRenderer getInstance() {
        return INSTANCE;
    }

    public void initialize() {
        WorldRenderEvents.BEFORE_TRANSLUCENT.register(this::extractAndDraw);
    }

    private void extractAndDraw(@NonNull WorldRenderContext context) {
        if (!hasAnyChamsEnabled()) {
            return;
        }

        extract(context);
        if (!hasPendingGeometry) {
            return;
        }
        draw(Minecraft.getInstance(), FILLED_THROUGH_WALLS);
    }

    private void extract(@NonNull WorldRenderContext context) {
        hasPendingGeometry = false;

        Minecraft client = Minecraft.getInstance();
        if (client.level == null || context.matrices() == null) {
            return;
        }

        PoseStack matrices = context.matrices();
        Vec3 camera = context.worldState().cameraRenderState.pos;

        matrices.pushPose();
        matrices.translate(-camera.x, -camera.y, -camera.z);

        if (buffer == null) {
            buffer = new BufferBuilder(ALLOCATOR, FILLED_THROUGH_WALLS.getVertexFormatMode(),
                    FILLED_THROUGH_WALLS.getVertexFormat());
        }

        BufferBuilder extractedBuffer = this.buffer;
        if (extractedBuffer == null) {
            matrices.popPose();
            return;
        }

        float alpha = Math.max(0.05f, Math.min(1.0f, StarClientOptions.mobChamsAlpha));
        var level = Objects.requireNonNull(client.level);
        for (Entity rawEntity : level.entitiesForRendering()) {
            if (rawEntity == null) {
                continue;
            }
            Entity entity = rawEntity;

            if (!shouldRenderChamsForEntity(client, entity, camera)) {
                continue;
            }

            float[] color = getColorForEntity(entity);
            AABB box = entity.getBoundingBox().inflate(0.05);
            renderFilledBox(matrices.last().pose(), extractedBuffer,
                    (float) box.minX,
                    (float) box.minY,
                    (float) box.minZ,
                    (float) box.maxX,
                    (float) box.maxY,
                    (float) box.maxZ,
                    color[0],
                    color[1],
                    color[2],
                    alpha);
            hasPendingGeometry = true;
        }

        matrices.popPose();
    }

    private static boolean shouldRenderChamsForEntity(@NonNull Minecraft client, @NonNull Entity entity,
            @NonNull Vec3 cameraPos) {
        if (entity == client.getCameraEntity() || !entity.isAlive()) {
            return false;
        }

        boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
        boolean isHostile = entity instanceof Enemy;
        boolean isItem = entity.getType() == EntityType.ITEM;
        boolean isNonHostile = !isItem && !isHostile && !isPlayer && entity instanceof LivingEntity;

        if (!isHostile && !isPlayer && !isItem && !isNonHostile) {
            return false;
        }

        double distSq = entity.distanceToSqr(cameraPos.x, cameraPos.y, cameraPos.z);

        if (isPlayer) {
            return StarClientOptions.chamsPlayer && distSq < StarClientOptions.chamsDistancePlayer;
        }
        if (isHostile) {
            return StarClientOptions.chamsHostile && distSq < StarClientOptions.chamsDistanceHostile;
        }
        if (isItem) {
            return StarClientOptions.chamsItem && distSq < StarClientOptions.chamsDistanceItem;
        }
        return StarClientOptions.chamsMob && distSq < StarClientOptions.chamsDistanceMob;
    }

    private static boolean hasAnyChamsEnabled() {
        return StarClientOptions.chamsPlayer
                || StarClientOptions.chamsHostile
                || StarClientOptions.chamsMob
                || StarClientOptions.chamsItem;
    }

    private static float[] getColorForEntity(@NonNull Entity entity) {
        float hue = getHueForEntity(entity);
        int rgb = Color.HSBtoRGB(Math.max(0.0f, Math.min(1.0f, hue)), 0.85f, 0.95f);
        float red = ((rgb >> 16) & 0xFF) / 255.0f;
        float green = ((rgb >> 8) & 0xFF) / 255.0f;
        float blue = (rgb & 0xFF) / 255.0f;
        return new float[] { red, green, blue };
    }

    private static float getHueForEntity(@NonNull Entity entity) {
        boolean isPlayer = entity.getType() == EntityType.PLAYER || entity instanceof Avatar;
        boolean isHostile = entity instanceof Enemy;
        boolean isItem = entity.getType() == EntityType.ITEM;
        if (isPlayer) {
            return StarClientOptions.chamsHuePlayer;
        }
        if (isHostile) {
            return StarClientOptions.chamsHueHostile;
        }
        if (isItem) {
            return StarClientOptions.chamsHueItem;
        }
        return StarClientOptions.chamsHueMob;
    }

    private void renderFilledBox(@NonNull Matrix4fc positionMatrix, @NonNull BufferBuilder buffer, float minX,
            float minY, float minZ,
            float maxX, float maxY, float maxZ, float red, float green, float blue, float alpha) {
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, maxY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, maxY, minZ).setColor(red, green, blue, alpha);

        buffer.addVertex(positionMatrix, minX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, minZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, maxX, minY, maxZ).setColor(red, green, blue, alpha);
        buffer.addVertex(positionMatrix, minX, minY, maxZ).setColor(red, green, blue, alpha);
    }

    private void draw(@NonNull Minecraft client, @NonNull RenderPipeline pipeline) {
        if (buffer == null) {
            return;
        }

        MeshData builtBuffer = buffer.buildOrThrow();
        MeshData.DrawState drawState = builtBuffer.drawState();
        VertexFormat format = drawState.format();

        GpuBuffer vertices = upload(drawState, format, builtBuffer);
        drawPass(client, pipeline, builtBuffer, drawState, vertices, format);

        if (vertexBuffer != null) {
            vertexBuffer.rotate();
        }
        buffer = null;
    }

    private @NonNull GpuBuffer upload(MeshData.DrawState drawState, @NonNull VertexFormat format,
            @NonNull MeshData builtBuffer) {
        int vertexBufferSize = drawState.vertexCount() * format.getVertexSize();

        if (vertexBuffer == null || vertexBuffer.size() < vertexBufferSize) {
            if (vertexBuffer != null) {
                vertexBuffer.close();
            }
            vertexBuffer = new MappableRingBuffer(() -> "starclient mob chams",
                    GpuBuffer.USAGE_VERTEX | GpuBuffer.USAGE_MAP_WRITE,
                    vertexBufferSize);
        }

        CommandEncoder encoder = RenderSystem.getDevice().createCommandEncoder();
        MappableRingBuffer ringBuffer = Objects.requireNonNull(vertexBuffer);
        ByteBuffer vertexData = Objects.requireNonNull(builtBuffer.vertexBuffer());
        GpuBuffer currentBuffer = Objects.requireNonNull(ringBuffer.currentBuffer());
        try (GpuBuffer.MappedView mappedView = encoder.mapBuffer(
                currentBuffer.slice(0, vertexData.remaining()), false, true)) {
            ByteBuffer mappedData = Objects.requireNonNull(mappedView.data());
            MemoryUtil.memCopy(vertexData, mappedData);
        }

        return currentBuffer;
    }

    private static void drawPass(@NonNull Minecraft client, @NonNull RenderPipeline pipeline,
            @NonNull MeshData builtBuffer,
            MeshData.DrawState drawState, @NonNull GpuBuffer vertices, @NonNull VertexFormat format) {
        GpuBuffer indices;
        VertexFormat.IndexType indexType;

        if (pipeline.getVertexFormatMode() == VertexFormat.Mode.QUADS) {
            builtBuffer.sortQuads(ALLOCATOR, RenderSystem.getProjectionType().vertexSorting());
            ByteBuffer indexBuffer = Objects.requireNonNull(builtBuffer.indexBuffer());
            indices = pipeline.getVertexFormat().uploadImmediateIndexBuffer(indexBuffer);
            indexType = builtBuffer.drawState().indexType();
        } else {
            RenderSystem.AutoStorageIndexBuffer shapeIndexBuffer = RenderSystem
                    .getSequentialBuffer(pipeline.getVertexFormatMode());
            indices = shapeIndexBuffer.getBuffer(drawState.indexCount());
            indexType = shapeIndexBuffer.type();
        }

        GpuBufferSlice dynamicTransforms = RenderSystem.getDynamicUniforms()
                .writeTransform(RenderSystem.getModelViewMatrix(), COLOR_MODULATOR, MODEL_OFFSET, TEXTURE_MATRIX);

        var colorTextureView = Objects.requireNonNull(client.getMainRenderTarget().getColorTextureView());
        var depthTextureView = Objects.requireNonNull(client.getMainRenderTarget().getDepthTextureView());

        try (RenderPass renderPass = RenderSystem.getDevice()
                .createCommandEncoder()
                .createRenderPass(() -> "starclient mob chams",
                        colorTextureView, Objects.requireNonNull(NO_STENCIL),
                        depthTextureView, Objects.requireNonNull(NO_CLEAR_DEPTH))) {
            renderPass.setPipeline(pipeline);
            RenderSystem.bindDefaultUniforms(renderPass);
            renderPass.setUniform("DynamicTransforms", dynamicTransforms);
            renderPass.setVertexBuffer(0, vertices);
            renderPass.setIndexBuffer(indices, indexType);
            renderPass.drawIndexed(0, 0, drawState.indexCount(), 1);
        }

        builtBuffer.close();
    }

    public void close() {
        ALLOCATOR.close();
        if (vertexBuffer != null) {
            vertexBuffer.close();
            vertexBuffer = null;
        }
        buffer = null;
        hasPendingGeometry = false;
    }
}
