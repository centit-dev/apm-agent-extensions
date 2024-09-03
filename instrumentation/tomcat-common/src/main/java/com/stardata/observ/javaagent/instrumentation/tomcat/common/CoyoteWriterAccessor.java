package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import java.lang.reflect.Field;

import org.apache.catalina.connector.CoyoteWriter;

public class CoyoteWriterAccessor {

    private static Field ERROR_FIELD;

    static {
        try {
            ERROR_FIELD = CoyoteWriter.class.getDeclaredField("error");
            ERROR_FIELD.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
            // ignored
        }
    }

    public static boolean getError(CoyoteWriter writer) {
        if (ERROR_FIELD == null) {
            return true;
        }
        try {
            return (boolean) ERROR_FIELD.get(writer);
        } catch (IllegalAccessException | ClassCastException ignored) {
            // we don't know this writer anymore, stop writing
            return true;
        }
    }

}
