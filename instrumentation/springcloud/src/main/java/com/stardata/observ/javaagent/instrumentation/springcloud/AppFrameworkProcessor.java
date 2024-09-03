package com.stardata.observ.javaagent.instrumentation.springcloud;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.util.VirtualField;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;

public class AppFrameworkProcessor implements SpanProcessor {
    public static final AttributeKey<String> APP_FRAMEWORK_ATTR_KEY = AttributeKey.stringKey("app.framework");
    public static String SPRING_CLOUD_APP_FRAMEWORK = "springcloud";
    public static final VirtualField<String, String> APP_FRAMEWORK =
            VirtualField.find(String.class, String.class);

    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        if (readWriteSpan.getKind() != SpanKind.SERVER) {
            return;
        }
        String scheme = readWriteSpan.getAttribute(AttributeKey.stringKey("url.scheme"));
        if (scheme != null && scheme.equals("http")) {
            if (APP_FRAMEWORK.get(SPRING_CLOUD_APP_FRAMEWORK) != null) {
                readWriteSpan.setAttribute(APP_FRAMEWORK_ATTR_KEY, SPRING_CLOUD_APP_FRAMEWORK);
            }
        }
    }

    @Override
    public boolean isStartRequired() {
        return true;
    }

    @Override
    public void onEnd(ReadableSpan readableSpan) {
    }

    @Override
    public boolean isEndRequired() {
        return false;
    }
}
