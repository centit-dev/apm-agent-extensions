package com.stardata.observ.javaagent.instrumentation.baggage;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import io.opentelemetry.api.baggage.Baggage;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class SpanBaggageProcessor implements SpanProcessor {

    private final List<String> keys;

    public SpanBaggageProcessor(List<String> keys) {
        this.keys = keys == null ? Collections.emptyList() : keys;
    }

    @Override
    public void onStart(@Nonnull Context parentContext, @Nonnull ReadWriteSpan span) {
        Baggage baggage = Baggage.fromContext(parentContext);
        if (baggage == Baggage.empty()) {
            return;
        }
        for (String keys : keys) {
            String value = baggage.getEntryValue(keys);
            if (value == null) {
                continue;
            }
            span.setAttribute(keys, value);
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(@Nonnull ReadableSpan span) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }

}
