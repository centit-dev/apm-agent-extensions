package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

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

import com.stardata.observ.javaagent.instrumentation.common.Constant;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class Tomcat10InstrumentationModuleTest {

    private static final TomcatServer server = new TomcatServer();

    private SimpleClient client;

    @RegisterExtension
    private static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

    @BeforeAll
    static void beforeClass() {
        server.start();
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
    void testAdviceParsingResponse() {
        testing.runWithSpan("start-get", () -> {
            client.request("GET", "/hello", "");
        });

        List<List<SpanData>> traces = testing.waitForTraces(2);
        SpanData span = traces.stream()
                .flatMap(List::stream)
                .filter(s -> s.getKind() == SpanKind.SERVER)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No span with name 'start-get' found"));
        AttributeKey<String> key = AttributeKey.stringKey(Constant.HTTP_RESPONSE_BODY);
        assertThat(span.getAttributes().get(key), equalTo("Hello, World!"));
    }

    @Test
    void testAdviceParsingRequest() {
        testing.runWithSpan("start-post", () -> {
            client.request("POST", "/hello", "ping");
        });

        List<List<SpanData>> traces = testing.waitForTraces(2);
        SpanData span = traces.stream()
                .flatMap(List::stream)
                .filter(s -> s.getKind() == SpanKind.SERVER)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No span with name 'start-post' found"));
        AttributeKey<String> key = AttributeKey.stringKey(Constant.HTTP_REQUEST_BODY);
        assertThat(span.getAttributes().get(key), equalTo("ping"));
    }

}
