package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.util.VirtualField;

public class SessionHelper {
    private static final VirtualField<String, String> SPANID_ADDRESS =
            VirtualField.find(String.class, String.class);
    private static final VirtualField<Context, String> CONSUMER_CONTEXT =
            VirtualField.find(Context.class, String.class);

    public static String getConsumerContext(Context ctx) {
        return CONSUMER_CONTEXT.get(ctx);
    }

    public static void putConsumerContext(Context ctx, String value) {
        CONSUMER_CONTEXT.set(ctx, value);
    }

    public static String getConsumerSpanID(String spanID) {
        return SPANID_ADDRESS.get(spanID.intern());
    }

    public static void putConsumerSpanID(String spanID, String value) {
        SPANID_ADDRESS.set(spanID.intern(), value.intern());
    }

    public static String getProducerInfo(String id) {
        return SPANID_ADDRESS.get(id);
    }

    public static void putProducerInfo(String id, String value) {
        SPANID_ADDRESS.set(id, value);
    }

    public static void clear(String id) {
        SPANID_ADDRESS.set(id, null);
    }
}
