package com.stardata.observ.javaagent.instrumentation.ttsever;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.instrumenter.AttributesExtractor;
import io.opentelemetry.semconv.ServerAttributes;
import io.opentelemetry.semconv.incubating.DbIncubatingAttributes;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

public class CauDriverAttributesExtractor implements AttributesExtractor<TtServerRequest, Void> {
    public static final AttributeKey<String> KEY = stringKey("db.query.key");

    @Override
    public void onStart(
            @Nonnull AttributesBuilder attributesBuilder,
            @Nonnull Context context,
            @Nonnull TtServerRequest ttServerRequest) {
        attributesBuilder.put(DbIncubatingAttributes.DB_SYSTEM, ttServerRequest.getSystem());
        attributesBuilder.put(DbIncubatingAttributes.DB_NAME, ttServerRequest.getSystem());
        attributesBuilder.put(DbIncubatingAttributes.DB_OPERATION, ttServerRequest.getCommand());
        attributesBuilder.put(KEY, ttServerRequest.getSpanKeyAttr());
    }

    @Override
    public void onEnd(
            @Nonnull AttributesBuilder attributesBuilder,
            @Nonnull Context context,
            @Nonnull TtServerRequest ttServerRequest,
            @Nullable Void unused,
            @Nullable Throwable throwable) {
        attributesBuilder.put(ServerAttributes.SERVER_ADDRESS, ttServerRequest.getHost());
        attributesBuilder.put(ServerAttributes.SERVER_PORT, ttServerRequest.getPort());
    }
}
