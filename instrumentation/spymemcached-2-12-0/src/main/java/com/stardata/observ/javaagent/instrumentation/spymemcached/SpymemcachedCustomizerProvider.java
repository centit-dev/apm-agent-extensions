package com.stardata.observ.javaagent.instrumentation.spymemcached;

import javax.annotation.Nonnull;

import com.google.auto.service.AutoService;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;

@AutoService(AutoConfigurationCustomizerProvider.class)
public class SpymemcachedCustomizerProvider implements AutoConfigurationCustomizerProvider {
    @Override
    public void customize(@Nonnull AutoConfigurationCustomizer autoConfiguration) {
        autoConfiguration.addTracerProviderCustomizer(this::configureSdkTracerProvider);
    }

    private SdkTracerProviderBuilder configureSdkTracerProvider(
            SdkTracerProviderBuilder tracerProvider, ConfigProperties config) {
        return tracerProvider.addSpanProcessor(new SpanMemcachedAddressProcessor());
    }
}
