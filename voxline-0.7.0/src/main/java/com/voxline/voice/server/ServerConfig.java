package com.voxline.voice.server;

import com.voxline.voice.VoxlineVoice;
import net.minecraftforge.fml.loading.FMLPaths;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Properties;

public final class ServerConfig {
    public static final ServerConfig INSTANCE = new ServerConfig();
    public boolean enabled = true;
    public boolean groupsEnabled = true;
    public double maxRange = 64.0;
    public int maxPacketsPerSecond = 70;
    public int maxGroupSize = 12;
    private final Path path = FMLPaths.CONFIGDIR.get().resolve("voxline-voice-server.properties");

    private ServerConfig() { }

    public synchronized void load() {
        if (!Files.exists(path)) { save(); return; }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            p.load(in);
            enabled = Boolean.parseBoolean(p.getProperty("enabled", "true"));
            groupsEnabled = Boolean.parseBoolean(p.getProperty("groupsEnabled", "true"));
            maxRange = boundedDouble(p.getProperty("maxRange"), 64.0, 4.0, 64.0);
            maxPacketsPerSecond = boundedInt(p.getProperty("maxPacketsPerSecond"), 70, 50, 100);
            maxGroupSize = boundedInt(p.getProperty("maxGroupSize"), 12, 2, 24);
        } catch (IOException e) {
            VoxlineVoice.LOGGER.warn("Could not read Voxline Voice server config", e);
        }
    }

    public synchronized void save() {
        try {
            Files.createDirectories(path.getParent());
            Properties p = new Properties();
            p.setProperty("enabled", Boolean.toString(enabled));
            p.setProperty("groupsEnabled", Boolean.toString(groupsEnabled));
            p.setProperty("maxRange", Double.toString(Math.max(4.0, Math.min(64.0, maxRange))));
            p.setProperty("maxPacketsPerSecond", Integer.toString(Math.max(50, Math.min(100, maxPacketsPerSecond))));
            p.setProperty("maxGroupSize", Integer.toString(Math.max(2, Math.min(24, maxGroupSize))));
            Path tmp = path.resolveSibling(path.getFileName() + ".tmp");
            try (OutputStream out = Files.newOutputStream(tmp)) { p.store(out, "Voxline Voice server policy"); }
            try { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE); }
            catch (IOException atomicUnsupported) { Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING); }
        } catch (IOException e) {
            VoxlineVoice.LOGGER.warn("Could not save Voxline Voice server config", e);
        }
    }

    private static int boundedInt(String value, int fallback, int min, int max) {
        try { return Math.max(min, Math.min(max, Integer.parseInt(value))); }
        catch (Exception ignored) { return fallback; }
    }
    private static double boundedDouble(String value, double fallback, double min, double max) {
        try { double v = Double.parseDouble(value); return Double.isFinite(v) ? Math.max(min, Math.min(max, v)) : fallback; }
        catch (Exception ignored) { return fallback; }
    }
}
