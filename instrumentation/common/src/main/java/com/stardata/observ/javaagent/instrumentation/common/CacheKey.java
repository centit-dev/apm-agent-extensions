package com.stardata.observ.javaagent.instrumentation.common;

import java.util.Objects;

import io.opentelemetry.api.trace.Span;

public class CacheKey {

    private final String traceId;
    private final String spanId;
    private final String action;

    public CacheKey(Span span) {
        traceId = span.getSpanContext().getTraceId();
        spanId = span.getSpanContext().getSpanId();
        action = "";
    }

    public CacheKey(Span span, String action) {
        traceId = span.getSpanContext().getTraceId();
        spanId = span.getSpanContext().getSpanId();
        this.action = action;
    }

    public CacheKey(String traceId, String spanId, String action) {
        this.traceId = traceId;
        this.spanId = spanId;
        this.action = action;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        CacheKey other = (CacheKey) obj;
        return Objects.equals(traceId, other.traceId)
                && Objects.equals(spanId, other.spanId)
                && Objects.equals(action, other.action);
    }

    @Override
    public int hashCode() {
        return Objects.hash(traceId, spanId, action);
    }

    @Override
    public String toString() {
        return String.format("CacheKey{traceId='%s', spanId='%s', action='%s'}", traceId, spanId, action);
    }

}
