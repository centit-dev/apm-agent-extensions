package com.stardata.observ.javaagent.instrumentation.ttsever;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.instrumentation.api.instrumenter.Instrumenter;

public final class CauDriverSingletons {
    private static final String INSTRUMENTATION_NAME = "io.opentelemetry.ttserver";
    private static final Instrumenter<TtServerRequest, Void> STATEMENT_INSTRUMENTER;

    static {
        STATEMENT_INSTRUMENTER =
                Instrumenter.<TtServerRequest, Void>builder(
                                GlobalOpenTelemetry.get(),
                                INSTRUMENTATION_NAME,
                                new CauDriverSpanNameExtractor()
                        ).addAttributesExtractor(new CauDriverAttributesExtractor())
                        .buildInstrumenter(new CauDriverSpanKindExtractor());
    }

    public static Instrumenter<TtServerRequest, Void> statementInstrumenter() {
        return STATEMENT_INSTRUMENTER;
    }

    private CauDriverSingletons() {
    }
}
