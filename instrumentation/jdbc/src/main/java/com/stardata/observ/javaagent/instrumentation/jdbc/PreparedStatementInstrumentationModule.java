package com.stardata.observ.javaagent.instrumentation.jdbc;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

@AutoService(InstrumentationModule.class)
public class PreparedStatementInstrumentationModule extends InstrumentationModule {

    public PreparedStatementInstrumentationModule() {
        super("jdbc", "jdbc-arguments");
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("com.stardata.observ.javaagent.instrumentation");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new PreparedStatementSetterInstrumentation(),
                new PreparedStatementExecutionInstrumentation());
    }

}
