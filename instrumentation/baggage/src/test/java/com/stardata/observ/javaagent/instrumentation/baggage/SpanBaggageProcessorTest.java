package com.stardata.observ.javaagent.instrumentation.baggage;

import java.util.Collections;
import java.util.List;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.sdk.trace.data.SpanData;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.Assert.assertNotNull;

public class SpanBaggageProcessorTest {

    private static final TomcatServer server = new TomcatServer();

    private SimpleClient client;

    @RegisterExtension
    private static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

    @BeforeAll
    static void beforeClass() {
        server.start();

        // setting the propagation keys in the gradle test task
    }

    @AfterAll
    static void afterClass() {
        server.stop();
    }

    @BeforeEach
    void setUp() {
        client = new SimpleClient(server.getAddress());
    }

    @Test
    void testWithBaggage() {
        testing.runWithSpan("baggage-span", () -> {
            client.request("GET", "/hello", Collections.singletonMap("baggage", "key1=value,key2=value,excluded=ignored"));
        });

        List<List<SpanData>> traces = testing.waitForTraces(2);
        SpanData span = traces.stream().flatMap(List::stream)
                .filter(s -> s.getKind() == SpanKind.SERVER)
                .findAny().orElse(null);
        assertNotNull(span);

        testing.waitAndAssertTraces(
            trace -> trace.singleElement().hasKind(SpanKind.INTERNAL),
            trace -> {
                AttributeKey<String> key1 = AttributeKey.stringKey("key1");
                AttributeKey<String> key2 = AttributeKey.stringKey("key2");
                trace.singleElement()
                        .hasKind(SpanKind.SERVER)
                        .hasAttribute(key1, "value")
                        .hasAttribute(key2, "value");
            }
        );
    }

}
