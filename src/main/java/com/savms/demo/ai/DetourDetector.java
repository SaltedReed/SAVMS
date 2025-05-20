package com.savms.demo.ai;

/**
 * Detect whether a driver is detouring or not
 */
public interface DetourDetector {
    boolean execute(DetourDetectCommand cmd);
}
