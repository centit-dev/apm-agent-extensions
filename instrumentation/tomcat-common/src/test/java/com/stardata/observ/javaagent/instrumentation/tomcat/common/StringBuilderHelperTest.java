package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import java.nio.charset.Charset;

import org.junit.jupiter.api.Test;

import com.stardata.observ.javaagent.instrumentation.common.CacheKey;

import static java.util.concurrent.ThreadLocalRandom.current;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class StringBuilderHelperTest {
    @Test
    void testSetCharsetUtf8() {
        CacheKey key = new CacheKey("traceId", "spanId#" + current().nextLong(1_000_000), "request");
        StringBuilderHelper.setCharset(key, "text/html; charset=UTF-8");
        Charset charset = StringBuilderHelper.CHARSET.get(key);
        assertEquals("UTF-8", charset.name());
    }

    @Test
    void testSetCharsetIso8859() {
        CacheKey key = new CacheKey("traceId", "spanId#" + current().nextLong(1_000_000), "request");
        StringBuilderHelper.setCharset(key, "application/json; charset=ISO-8859-1");
        Charset charset = StringBuilderHelper.CHARSET.get(key);
        assertEquals("ISO-8859-1", charset.name());
    }

    @Test
    void testSetCharsetNoCharset() {
        CacheKey key = new CacheKey("traceId", "spanId#" + current().nextLong(1_000_000), "request");
        StringBuilderHelper.setCharset(key, "text/html");
        Charset charset = StringBuilderHelper.CHARSET.get(key);
        assertNull(charset);
    }
}
