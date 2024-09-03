package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import javax.jms.Message;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.javaagent.bootstrap.internal.ExperimentalConfig;

public final class ActivemqReceiveSpanUtil {
    private static final ContextPropagators propagators = GlobalOpenTelemetry.getPropagators();
    private static final boolean receiveInstrumentationEnabled =
            ExperimentalConfig.get().messagingReceiveInstrumentationEnabled();

    public static Context getParentContext(Message message) {
        Context parentContext = Context.current();
        if (!receiveInstrumentationEnabled) {
            parentContext =
                    propagators
                            .getTextMapPropagator()
                            .extract(parentContext, message, MessagePropertyGetter.INSTANCE);
        }
        return parentContext;
    }

    public static String getSpanID(Message message) {
        Context parentContext = Context.current();
        if (!receiveInstrumentationEnabled) {
            parentContext =
                    propagators
                            .getTextMapPropagator()
                            .extract(parentContext, message, MessagePropertyGetter.INSTANCE);
        }
        Span span = Span.fromContext(parentContext);
        if (span != null) {
            return span.getSpanContext().getSpanId();
        } else {
            return "";
        }
    }

    private ActivemqReceiveSpanUtil() {
    }
}
