package com.odschool.util;

import org.slf4j.MDC;

public final class TraceIdUtil {
    private static final String MDC_KEY = "eventTraceId";

    public static void setTraceId(String traceId) {
        MDC.put(MDC_KEY, traceId);
    }
    public static String getTraceId() {
        return MDC.get(MDC_KEY);
    }

    public static void clear() {
        MDC.remove(MDC_KEY);
    }
}