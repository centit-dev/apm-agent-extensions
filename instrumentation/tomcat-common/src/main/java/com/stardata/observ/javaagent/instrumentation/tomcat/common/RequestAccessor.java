package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import java.lang.reflect.Field;

import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;

public class RequestAccessor {

    private static Field REQUEST_FIELD;
    private static Field USING_INPUT_STREAM;

    static {
        try {
            REQUEST_FIELD = RequestFacade.class.getDeclaredField("request");
            REQUEST_FIELD.setAccessible(true);

            USING_INPUT_STREAM = Request.class.getDeclaredField("usingInputStream");
            USING_INPUT_STREAM.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
            // ignored
        }
    }

    public static boolean usingInputStream(Request request) {
        if (REQUEST_FIELD == null || USING_INPUT_STREAM == null) {
            return false;
        }

        try {
            return (boolean) USING_INPUT_STREAM.get(request);
        } catch (IllegalAccessException | ClassCastException ignored) {
            return false;
        }
    }

    public static boolean usingInputStream(RequestFacade request) {
        if (REQUEST_FIELD == null || USING_INPUT_STREAM == null) {
            return false;
        }

        try {
            Request requestObj = (Request) REQUEST_FIELD.get(request);
            return (boolean) USING_INPUT_STREAM.get(requestObj);
        } catch (IllegalAccessException | ClassCastException ignored) {
            return false;
        }
    }

}
