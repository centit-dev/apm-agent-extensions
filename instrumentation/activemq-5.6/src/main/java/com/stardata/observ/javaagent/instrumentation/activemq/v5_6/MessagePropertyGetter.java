package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import java.util.Collections;

import javax.jms.Message;

import io.opentelemetry.context.propagation.TextMapGetter;

enum MessagePropertyGetter implements TextMapGetter<Message> {
    INSTANCE;

    @Override
    public Iterable<String> keys(Message message) {
        try {
            return Collections.list(message.getPropertyNames());
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @Override
    public String get(Message carrier, String key) {
        String propName = key.replace("-", "__dash__");
        Object value;
        try {
            value = carrier.getObjectProperty(propName);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
        if (value instanceof String) {
            return (String) value;
        } else {
            return null;
        }
    }
}
