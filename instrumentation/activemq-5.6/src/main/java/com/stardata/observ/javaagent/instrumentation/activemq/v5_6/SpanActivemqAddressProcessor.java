package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.trace.ReadWriteSpan;
import io.opentelemetry.sdk.trace.ReadableSpan;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.semconv.incubating.MessagingIncubatingAttributes;

import static com.stardata.observ.javaagent.instrumentation.activemq.v5_6.ActivemqConst.ACTIVEMQ_ADDRESS;

public class SpanActivemqAddressProcessor implements SpanProcessor {
    @Override
    public void onStart(Context context, ReadWriteSpan readWriteSpan) {
        String sys = readWriteSpan.getAttribute(MessagingIncubatingAttributes.MESSAGING_SYSTEM);
        if (sys != null && sys.equals("jms")) {
            if (readWriteSpan.getKind() == SpanKind.PRODUCER) {
                SessionHelper.putProducerInfo(readWriteSpan.getSpanContext().getSpanId(), "");
            }
            if (readWriteSpan.getKind() == SpanKind.CONSUMER) {
                String address = SessionHelper.getConsumerContext(context);
                if (address != null) {
                    readWriteSpan.setAttribute(ACTIVEMQ_ADDRESS, address);
                    return;
                }
                address = SessionHelper.getConsumerSpanID(readWriteSpan.getParentSpanContext().getSpanId());
                if (address != null) {
                    readWriteSpan.setAttribute(ACTIVEMQ_ADDRESS, address);
                }
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
