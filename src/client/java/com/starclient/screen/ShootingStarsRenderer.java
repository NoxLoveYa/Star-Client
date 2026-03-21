package com.starclient.screen;

import net.minecraft.client.gui.GuiGraphics;
import org.jspecify.annotations.NonNull;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class ShootingStarsRenderer {
    private static final float FIXED_STEP_SECONDS = 1f / 120f;
    private static final int MAX_STEPS_PER_FRAME = 4;
    private static final float LIFETIME_FADE_PORTION = 0.4f;
    private static final boolean ANTIALIAS_TRAILS = false;
    private static final int TRAIL_CHUNK_COUNT = 5;

    private final Random random = new Random();
    private final List<ShootingStar> shootingStars = new ArrayList<>();
    private float spawnDensity = 1.0f;
    private float minStarSize = 1.0f;
    private float maxStarSize = 2.0f;
    private float minHue = 0.0f;
    private float maxHue = 1.0f;

    private long lastFrameNanos = -1L;
    private float updateAccumulatorSeconds = 0f;
    private float spawnTimer = 0f;
    private float nextSpawnDelay = randomSpawnDelay();

    public void render(@NonNull GuiGraphics context, int width, int height) {
        updateStars(width, height);
        renderStars(context);
    }

    public void setSpawnDensity(float spawnDensity) {
        this.spawnDensity = Math.max(0f, Math.min(200f, spawnDensity));
    }

    public void setSizeRange(float minSize, float maxSize) {
        float clampedMin = Math.max(0.5f, minSize);
        float clampedMax = Math.max(0.5f, maxSize);
        if (clampedMin > clampedMax) {
            float temp = clampedMin;
            clampedMin = clampedMax;
            clampedMax = temp;
        }
        this.minStarSize = clampedMin;
        this.maxStarSize = clampedMax;
    }

    public void setHueRange(float minHue, float maxHue) {
        float clampedMin = Math.max(0f, Math.min(1f, minHue));
        float clampedMax = Math.max(0f, Math.min(1f, maxHue));
        if (clampedMin > clampedMax) {
            float temp = clampedMin;
            clampedMin = clampedMax;
            clampedMax = temp;
        }
        this.minHue = clampedMin;
        this.maxHue = clampedMax;
    }

    private void updateStars(int width, int height) {
        long now = System.nanoTime();
        if (lastFrameNanos < 0L) {
            lastFrameNanos = now;
            return;
        }

        float deltaSeconds = Math.min((now - lastFrameNanos) / 1_000_000_000f, 0.1f);
        lastFrameNanos = now;

        updateAccumulatorSeconds += deltaSeconds;

        int steps = 0;
        while (updateAccumulatorSeconds >= FIXED_STEP_SECONDS && steps < MAX_STEPS_PER_FRAME) {
            simulateStep(width, height, FIXED_STEP_SECONDS);
            updateAccumulatorSeconds -= FIXED_STEP_SECONDS;
            steps++;
        }

        if (steps >= MAX_STEPS_PER_FRAME) {
            updateAccumulatorSeconds = 0f;
        }
    }

    private void simulateStep(int width, int height, float stepSeconds) {
        int maxActiveStars = calculateMaxActiveStars();

        spawnTimer += stepSeconds;
        int maxSpawnsThisStep = Math.max(1, Math.min(12, 1 + Math.round(spawnDensity / 10f)));
        int spawnedThisStep = 0;
        while (spawnTimer >= nextSpawnDelay && spawnedThisStep < maxSpawnsThisStep
                && shootingStars.size() < maxActiveStars) {
            spawnTimer -= nextSpawnDelay;
            nextSpawnDelay = randomSpawnDelay();
            shootingStars.add(createShootingStar(width, height));
            spawnedThisStep++;
        }

        Iterator<ShootingStar> iterator = shootingStars.iterator();
        while (iterator.hasNext()) {
            ShootingStar star = iterator.next();
            star.lifeSeconds -= stepSeconds;
            if (star.lifeSeconds <= 0f) {
                iterator.remove();
                continue;
            }

            star.previousX = star.x;
            star.previousY = star.y;

            star.velocityY += star.dropPerSecond * stepSeconds;
            star.x += star.velocityX * stepSeconds;
            star.y += star.velocityY * stepSeconds;

            float cullMargin = star.trailLength + 8f;
            if (star.x < -cullMargin || star.x > width + cullMargin
                    || star.y < -cullMargin || star.y > height + cullMargin) {
                iterator.remove();
            }
        }

        while (shootingStars.size() > maxActiveStars) {
            shootingStars.remove(0);
        }
    }

    private void renderStars(@NonNull GuiGraphics context) {
        for (ShootingStar star : shootingStars) {
            float vx = star.x - star.previousX;
            float vy = star.y - star.previousY;
            float speed = (float) Math.sqrt(vx * vx + vy * vy);
            if (speed <= 0.001f) {
                vx = star.velocityX * FIXED_STEP_SECONDS;
                vy = star.velocityY * FIXED_STEP_SECONDS;
                speed = (float) Math.sqrt(vx * vx + vy * vy);
            }
            if (speed <= 0.001f) {
                continue;
            }

            float directionX = vx / speed;
            float directionY = vy / speed;
            float visibility = computeLifeFade(star);
            if (visibility <= 0f) {
                continue;
            }

            drawSolidTrail(context, star, directionX, directionY, visibility);
            drawRoundedGlowingHead(context, star, visibility);
        }
    }

    private float computeLifeFade(ShootingStar star) {
        float maxLife = Math.max(0.001f, star.maxLifeSeconds);
        float remainingRatio = Math.max(0f, Math.min(1f, star.lifeSeconds / maxLife));
        if (remainingRatio >= LIFETIME_FADE_PORTION) {
            return 1f;
        }
        return remainingRatio / LIFETIME_FADE_PORTION;
    }

    private void drawSolidTrail(@NonNull GuiGraphics context, ShootingStar star, float directionX, float directionY,
            float visibilityRatio) {
        float tailX = star.x - directionX * star.trailLength;
        float tailY = star.y - directionY * star.trailLength;

        int chunkCount = TRAIL_CHUNK_COUNT;
        for (int chunk = 0; chunk < chunkCount; chunk++) {
            float startRatio = chunk / (float) chunkCount;
            float endRatio = (chunk + 1f) / chunkCount;

            float x0 = lerp(star.x, tailX, startRatio);
            float y0 = lerp(star.y, tailY, startRatio);
            float x1 = lerp(star.x, tailX, endRatio);
            float y1 = lerp(star.y, tailY, endRatio);

            float midRatio = (startRatio + endRatio) * 0.5f;
            float intensity = (float) Math.pow(1f - midRatio, 1.25f) * visibilityRatio * 0.42f;
            int alpha = (int) (Math.max(0f, Math.min(1f, intensity)) * 255f);
            if (alpha <= 0) {
                continue;
            }

            float thicknessScale = Math.max(0.45f, 1f - (midRatio * 0.75f));
            int thickness = Math.max(2, Math.min(4, Math.round(star.headSize * 0.9f * thicknessScale)));
            drawFastLineSegment(context, x0, y0, x1, y1, thickness, colorWithAlpha(star.rgbColor, alpha));
        }
    }

    private void drawFastLineSegment(@NonNull GuiGraphics context, float x0, float y0, float x1, float y1,
            int thickness, int color) {
        float dx = x1 - x0;
        float dy = y1 - y0;
        float maxDelta = Math.max(Math.abs(dx), Math.abs(dy));
        float stepPixels = Math.max(1f, thickness * 1.35f);
        int steps = Math.max(1, (int) Math.ceil(maxDelta / stepPixels));

        if (!ANTIALIAS_TRAILS) {
            int half = Math.max(0, thickness / 2);
            for (int i = 0; i <= steps; i++) {
                float t = i / (float) steps;
                int px = Math.round(x0 + dx * t);
                int py = Math.round(y0 + dy * t);
                context.fill(px - half, py - half, px - half + thickness, py - half + thickness, color);
            }
            return;
        }

        float radius = Math.max(0.75f, (thickness * 0.5f) + 0.35f);

        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            float px = x0 + dx * t;
            float py = y0 + dy * t;
            drawFilledCircle(context, Math.round(px), Math.round(py), radius, color);
        }
    }

    private float lerp(float a, float b, float t) {
        return a + ((b - a) * t);
    }

    private void drawRoundedGlowingHead(@NonNull GuiGraphics context, ShootingStar star, float visibilityRatio) {
        int centerX = Math.round(star.x);
        int centerY = Math.round(star.y);

        float coreRadius = Math.max(1.6f, star.headSize * 0.9f);
        int coreAlpha = (int) (Math.max(0f, Math.min(1f, visibilityRatio)) * 255f);

        int glowAlpha1 = (int) (coreAlpha * 0.26f);
        int glowAlpha2 = (int) (coreAlpha * 0.14f);
        int glowAlpha3 = (int) (coreAlpha * 0.07f);

        drawFilledCircle(context, centerX, centerY, coreRadius * 2.6f, colorWithAlpha(star.rgbColor, glowAlpha3));
        drawFilledCircle(context, centerX, centerY, coreRadius * 2.0f, colorWithAlpha(star.rgbColor, glowAlpha2));
        drawFilledCircle(context, centerX, centerY, coreRadius * 1.4f, colorWithAlpha(star.rgbColor, glowAlpha1));
        drawFilledCircle(context, centerX, centerY, coreRadius, colorWithAlpha(star.rgbColor, coreAlpha));

        int whiteCoreAlpha = (int) (coreAlpha * 0.75f);
        drawFilledCircle(context, centerX, centerY, Math.max(0.95f, coreRadius * 0.45f),
                colorWithAlpha(0x00FFFFFF, whiteCoreAlpha));
    }

    private void drawFilledCircle(@NonNull GuiGraphics context, int centerX, int centerY, float radius, int color) {
        if (radius <= 0f) {
            return;
        }

        int intRadius = (int) Math.ceil(radius);
        float radiusSq = radius * radius;

        for (int y = -intRadius; y <= intRadius; y++) {
            float yy = y * y;
            if (yy > radiusSq) {
                continue;
            }

            int xSpan = (int) Math.floor(Math.sqrt(radiusSq - yy));
            context.fill(centerX - xSpan, centerY + y, centerX + xSpan + 1, centerY + y + 1, color);
        }
    }

    private ShootingStar createShootingStar(int width, int height) {
        float startX = -(8f + random.nextFloat() * 28f);
        float startY = random.nextFloat() * height;
        float life = 1.2f + random.nextFloat() * 1.0f;
        float baseTrailLength = 22f + random.nextFloat() * 26f;
        float headSize = minStarSize + random.nextFloat() * (maxStarSize - minStarSize);
        float sizeRange = Math.max(0.001f, maxStarSize - minStarSize);
        float sizeNormalized = Math.max(0f, Math.min(1f, (headSize - minStarSize) / sizeRange));
        float speed = (460f + random.nextFloat() * 460f) * (0.72f + sizeNormalized * 0.92f);
        float heading = (float) (-0.5f + random.nextFloat() * 0.32f);
        float velocityX = (float) Math.cos(heading) * speed;
        float velocityY = (float) Math.sin(heading) * speed;
        float dropPerSecond = 35f + random.nextFloat() * 50f;

        float speedFactor = Math.max(0f, Math.min(1.2f, (speed - 220f) / 520f));
        float trailLength = baseTrailLength * (0.9f + speedFactor * 0.9f);

        float hue = minHue + random.nextFloat() * (maxHue - minHue);
        float saturation = 0.22f + random.nextFloat() * 0.18f;
        float brightness = 0.96f + random.nextFloat() * 0.04f;
        int rgbColor = Color.HSBtoRGB(hue, saturation, Math.min(1f, brightness)) & 0x00FFFFFF;

        return new ShootingStar(startX, startY, life, trailLength, headSize, rgbColor,
                velocityX, velocityY, dropPerSecond);
    }

    private int colorWithAlpha(int rgbColor, int alpha) {
        int clampedAlpha = Math.max(0, Math.min(255, alpha));
        return (clampedAlpha << 24) | (rgbColor & 0x00FFFFFF);
    }

    private float randomSpawnDelay() {
        if (spawnDensity <= 0f) {
            return Float.MAX_VALUE;
        }
        float baseDelay = 0.3f + random.nextFloat() * 1.2f;
        return baseDelay / spawnDensity;
    }

    private int calculateMaxActiveStars() {
        return Math.max(0, Math.min(80, 4 + Math.round(spawnDensity * 1.6f)));
    }

    private static class ShootingStar {
        private float x;
        private float y;
        private float previousX;
        private float previousY;
        private float lifeSeconds;
        private final float maxLifeSeconds;
        private final float trailLength;
        private final float headSize;
        private final int rgbColor;
        private final float velocityX;
        private float velocityY;
        private final float dropPerSecond;

        private ShootingStar(float x, float y, float lifeSeconds,
                float trailLength, float headSize, int rgbColor,
                float velocityX, float velocityY, float dropPerSecond) {
            this.x = x;
            this.y = y;
            this.previousX = x;
            this.previousY = y;
            this.lifeSeconds = lifeSeconds;
            this.maxLifeSeconds = lifeSeconds;
            this.trailLength = trailLength;
            this.headSize = headSize;
            this.rgbColor = rgbColor;
            this.velocityX = velocityX;
            this.velocityY = velocityY;
            this.dropPerSecond = dropPerSecond;
        }
    }

}