package com.stardata.observ.javaagent.instrumentation.jdbc;

import java.sql.PreparedStatement;
import java.util.Map;
import java.util.TreeMap;

import io.opentelemetry.instrumentation.api.util.VirtualField;

public class PreparedStatementArgumentHelper {

    private static final VirtualField<PreparedStatement, Map<Integer, Object>> ARGUMENTS =
            VirtualField.find(PreparedStatement.class, Map.class);

    public static Map<Integer, Object> get(PreparedStatement statement) {
        return ARGUMENTS.get(statement);
    }

    public static void put(PreparedStatement statement, Integer index, Object value) {
        Map<Integer, Object> arguments = get(statement);
        if (arguments == null) {
            arguments = new TreeMap<>();
            ARGUMENTS.set(statement, arguments);
        }
        arguments.put(index, value);
    }

    public static void clear(PreparedStatement statement) {
        ARGUMENTS.set(statement, null);
    }

}
