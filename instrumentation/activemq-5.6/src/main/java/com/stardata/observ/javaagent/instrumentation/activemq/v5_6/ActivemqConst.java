package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import io.opentelemetry.api.common.AttributeKey;

public final class ActivemqConst {
    public static final AttributeKey<String> ACTIVEMQ_ADDRESS = AttributeKey.stringKey("messaging.activemq.broker_address");
    public static final String SESSION_FIELD_NAME = "session";
}
