package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.stardata.observ.javaagent.instrumentation.common.CacheKey;

public class StringBuilderHelper {

    private static final int MAX_CACHE_SIZE = 1024 * 10; // 1KB

    private static final Pattern CHARSET_PATTERN = Pattern.compile("^.+?charset=(?<charset>.+?)(\\s|$)");

    // visible for testing
    static final Map<CacheKey, Charset> CHARSET = new ConcurrentHashMap<>();

    public static void append(StringBuilder body, byte[] buffer, int offset, int length) {
        if (body.length() + length > MAX_CACHE_SIZE) {
            return;
        }
        body.append(new String(buffer, offset, length, StandardCharsets.UTF_8));
    }

    public static void append(StringBuilder body, ByteBuffer buffer) {
        CharBuffer charBuffer = buffer.asCharBuffer();
        if (body.length() + charBuffer.remaining() > MAX_CACHE_SIZE) {
            return;
        }
        body.append(charBuffer, charBuffer.position(), charBuffer.remaining());
    }

    public static void append(StringBuilder body, boolean chars, Object ...args) {
        append(body, StandardCharsets.UTF_8, chars, args);
    }

    public static void append(StringBuilder body, int val) {
        char[] chars = Character.toChars(val);
        body.append(chars);
    }

    /**
     * @return if the value is appendable
     */
    private static boolean append(StringBuilder builder, Charset charset, boolean chars, Object ...args) {
        Object value = args[0];
        if (value instanceof Integer && chars) {
            // write int as char
            if (builder.length() + 1 > MAX_CACHE_SIZE) {
                return false;
            }
            builder.append((char) value);
            return true;
        }

        if (value instanceof Integer && !chars) {
            // write int as byte
            if (builder.length() + 1 > MAX_CACHE_SIZE) {
                return false;
            }
            builder.append(new String(new byte[] {(byte) value}, charset));
            return true;
        }

        if (value instanceof ByteBuffer) {
            CharBuffer buffer = ((ByteBuffer) value).asCharBuffer();
            if (builder.length() + buffer.remaining() > MAX_CACHE_SIZE) {
                return false;
            }
            builder.append(buffer, buffer.position(), buffer.remaining());
            return true;
        }

        if (args.length != 3 || !(args[1] instanceof Integer) || !(args[2] instanceof Integer)) {
            return true;
        }
        int offset = (int) args[1];
        int length = (int) args[2];

        if (builder.length() + length > MAX_CACHE_SIZE) {
            return false;
        }

        if (value instanceof char[]) {
            // write char[] with offset and length
            builder.append((char[]) value, offset, length);
            return true;
            // skip write(char[])
        }

        if (value instanceof String) {
            // write String with offset and length
            builder.append((String) value, offset, length);
            return true;
            // skip write(String)
        }

        if (value instanceof byte[]) {
            // write byte[] with offset and length
            builder.append(new String((byte[]) value, offset, length, charset));
            // skip write(byte[])
            return true;
        }

        return false;
    }

    public static void setCharset(CacheKey key, String encoding) {
        if (encoding == null) {
            return;
        }
        Matcher matcher = CHARSET_PATTERN.matcher(encoding);
        if (!matcher.find()) {
            return;
        }

        String charset = matcher.group("charset");
        CHARSET.put(key, Charset.forName(charset));
    }

    public static void clear(CacheKey key) {
        CHARSET.remove(key);
    }

}
