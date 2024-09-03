package com.stardata.observ.javaagent.instrumentation.spymemcached;

import javax.annotation.Nonnull;

import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.semconv.incubating.DbIncubatingAttributes;

public class SpanMemcachedAddressProcessor implements SpanProcessor {
    // use DB_CONNECTION_STRING for now
    @SuppressWarnings("deprecation")
    @Override
    public void onStart(@Nonnull Context context, @Nonnull ReadWriteSpan readWriteSpan) {
        String sys = readWriteSpan.getAttribute(DbIncubatingAttributes.DB_SYSTEM);
        if (sys != null && sys.equals("memcached")) {
            String socketAddr = OperationFutureHelper.get(context);
            if (socketAddr != null) {
                readWriteSpan.setAttribute(DbIncubatingAttributes.DB_CONNECTION_STRING, socketAddr);
            }
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(@Nonnull ReadableSpan readableSpan) {

    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
