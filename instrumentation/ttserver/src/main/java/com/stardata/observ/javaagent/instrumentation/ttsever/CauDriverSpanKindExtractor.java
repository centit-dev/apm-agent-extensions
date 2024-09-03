package com.stardata.observ.javaagent.instrumentation.ttsever;

import java.util.Objects;

import javax.annotation.Nonnull;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.api.instrumenter.SpanKindExtractor;

public class CauDriverSpanKindExtractor implements SpanKindExtractor<TtServerRequest> {

    @Override
    public SpanKind extract(@Nonnull TtServerRequest request) {
        if (Objects.equals(request.getFuncName(), CauDriverValues.SET_FUNCTION_NAME) ||
                Objects.equals(request.getFuncName(), CauDriverValues.DELETE_FUNCTION_NAME)) {
            return SpanKind.INTERNAL;
        } else {
            return SpanKind.CLIENT;
        }
    }
}
