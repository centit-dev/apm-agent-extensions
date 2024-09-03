package com.stardata.observ.javaagent.instrumentation.common;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonCodec {

    private static JsonCodec INSTANCE;

    private final ObjectMapper mapper;

    private JsonCodec() {
        mapper = new ObjectMapper();
    }

    public static JsonCodec getInstance() {
        if (INSTANCE == null) {
            synchronized (JsonCodec.class) {
                if (INSTANCE == null) {
                    INSTANCE = new JsonCodec();
                }
            }
        }

        return INSTANCE;
    }

    public String serialize(Object object) {
        try {
            return mapper.writeValueAsString(object);
        } catch (Exception e) {
            return null;
        }
    }

}
