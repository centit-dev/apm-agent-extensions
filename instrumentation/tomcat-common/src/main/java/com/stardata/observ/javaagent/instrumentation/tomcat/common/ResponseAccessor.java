package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import java.lang.reflect.Field;

import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;

public class ResponseAccessor {

    private static Field RESPONSE_FIELD;
    private static Field USING_OUTPUT_STREAM;

    static {
        try {
            RESPONSE_FIELD = ResponseFacade.class.getDeclaredField("response");
            RESPONSE_FIELD.setAccessible(true);

            USING_OUTPUT_STREAM = Response.class.getDeclaredField("usingOutputStream");
            USING_OUTPUT_STREAM.setAccessible(true);
        } catch (NoSuchFieldException ignored) {
            // ignored
        }
    }

    public static boolean usingOutputStream(Response response) {
        if (RESPONSE_FIELD == null || USING_OUTPUT_STREAM == null) {
            return false;
        }

        try {
            return (boolean) USING_OUTPUT_STREAM.get(response);
        } catch (IllegalAccessException | ClassCastException ignored) {
            return false;
        }
    }

    public static boolean usingOutputStream(ResponseFacade response) {
        if (RESPONSE_FIELD == null || USING_OUTPUT_STREAM == null) {
            return false;
        }

        try {
            Response responseObj = (Response) RESPONSE_FIELD.get(response);
            return (boolean) USING_OUTPUT_STREAM.get(responseObj);
        } catch (IllegalAccessException | ClassCastException ignored) {
            return false;
        }
    }

}
