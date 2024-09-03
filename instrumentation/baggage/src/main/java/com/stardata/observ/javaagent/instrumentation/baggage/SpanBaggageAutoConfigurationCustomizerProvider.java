package com.stardata.observ.javaagent.instrumentation.baggage;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class SpanBaggageAutoConfigurationCustomizerProvider
        implements AutoConfigurationCustomizerProvider {

    // visible for testing
    static final String PROPAGATOR_KEYS = "otel.propagators.keys";

    @Override
    public void customize(@Nonnull AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addTracerProviderCustomizer(this::configureSdkTracerProvider);
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
            SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
        List<String> keys = config.getList(PROPAGATOR_KEYS);
        return tracerProvider.addSpanProcessor(new SpanBaggageProcessor(keys));
    }

}
