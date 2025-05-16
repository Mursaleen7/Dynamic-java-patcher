package com.example.patcher.agent;

import java.util.List;

/**
 * Represents a manifest of patches to be applied.
 */
public class PatchManifest {
    private String version;
    private long timestamp;
    private List<PatchEntry> patches;

    public PatchManifest() {
        // Default constructor for Jackson
    }

    public PatchManifest(String version, long timestamp, List<PatchEntry> patches) {
        this.version = version;
        this.timestamp = timestamp;
        this.patches = patches;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public List<PatchEntry> getPatches() {
        return patches;
    }

    public void setPatches(List<PatchEntry> patches) {
        this.patches = patches;
    }
} 