package com.stardata.observ.javaagent.instrumentation.spymemcached;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.util.VirtualField;

public class OperationFutureHelper {
    private static final VirtualField<Context, String> OPERATION =
            VirtualField.find(Context.class, String.class);

    public static String get(Context ctx) {
        return OPERATION.get(ctx);
    }

    public static void put(Context ctx, String value) {
        OPERATION.set(ctx, value);
    }

    public static void clear(Context ctx) {
        OPERATION.set(ctx, null);
    }
}
