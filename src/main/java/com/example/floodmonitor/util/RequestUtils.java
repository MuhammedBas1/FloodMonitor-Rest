package com.example.floodmonitor.util;

import com.example.floodmonitor.exception.BadRequestException;
import com.example.floodmonitor.model.WarningLevel;

public final class RequestUtils {

    private RequestUtils() {}

    public static WarningLevel parseWarningLevel(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return WarningLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(
                    "Ungültige Warnstufe: '" + value + "'. Erlaubt: NORMAL, WARNING, CRITICAL, UNKNOWN");
        }
    }
}