package com.starclient.render;

import com.starclient.StarClientOptions;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;

import java.util.Objects;
import java.util.Random;

public final class LocalPlayerStarAuraRenderer {
    private static final Random RANDOM = new Random();
    private static int tickCounter = 0;

    private LocalPlayerStarAuraRenderer() {
    }

    public static void tick(Minecraft client) {
        if (!StarClientOptions.localPlayerStarAura) {
            return;
        }

        LocalPlayer player = client.player;
        if (player == null || client.level == null || !player.isAlive() || player.isSpectator()) {
            return;
        }

        tickCounter++;
        if ((tickCounter & 1) != 0) {
            return;
        }

        double baseY = player.getY() + (player.getBbHeight() * 0.45);
        double centerX = player.getX();
        double centerZ = player.getZ();
        double baseAngle = player.tickCount * 0.23;

        for (int i = 0; i < 2; i++) {
            double angle = baseAngle + (Math.PI * i) + (RANDOM.nextDouble() - 0.5) * 0.45;
            double radius = 0.45 + RANDOM.nextDouble() * 0.25;

            double x = centerX + Math.cos(angle) * radius;
            double y = baseY + (RANDOM.nextDouble() - 0.5) * 0.32;
            double z = centerZ + Math.sin(angle) * radius;

            double vx = (x - centerX) * 0.02;
            double vy = 0.01 + RANDOM.nextDouble() * 0.016;
            double vz = (z - centerZ) * 0.02;

            Objects.requireNonNull(client.level).addParticle(ParticleTypes.GLOW, x, y, z, vx, vy, vz);

            if (RANDOM.nextFloat() < 0.35f) {
                Objects.requireNonNull(client.level).addParticle(ParticleTypes.CHERRY_LEAVES, x, y, z, vx * 0.4, vy * 0.3, vz * 0.4);
            }
        }
    }
}