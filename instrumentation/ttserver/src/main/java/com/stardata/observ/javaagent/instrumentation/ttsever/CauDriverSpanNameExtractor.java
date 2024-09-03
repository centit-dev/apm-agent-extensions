package com.stardata.observ.javaagent.instrumentation.ttsever;

import javax.annotation.Nonnull;

import io.opentelemetry.instrumentation.api.instrumenter.SpanNameExtractor;

public class CauDriverSpanNameExtractor implements SpanNameExtractor<TtServerRequest> {

    @Override
    public String extract(@Nonnull TtServerRequest request) {
        return "CauBufferedDriver." + request.getFuncName();
    }
}
