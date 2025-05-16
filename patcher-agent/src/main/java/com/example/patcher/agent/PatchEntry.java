package com.example.patcher.agent;

/**
 * Represents a single patch entry for a class.
 */
public class PatchEntry {
    private String className;
    private String path;

    public PatchEntry() {
        // Default constructor for Jackson
    }

    public PatchEntry(String className, String path) {
        this.className = className;
        this.path = path;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
} 