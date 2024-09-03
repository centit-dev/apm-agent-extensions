package com.stardata.observ.javaagent.instrumentation.ttsever;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

@AutoService(InstrumentationModule.class)
public class CauDriverInstrumentationModule extends InstrumentationModule {
    public CauDriverInstrumentationModule() {
        super("ttserver", "ttserver-arguments");
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("com.stardata.observ.javaagent.instrumentation");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return Arrays.asList(
                new CauDriverSocketPoolInstrumentation(),
                new CauDriverQueryInstrumentation(),
                new CauDriverSetInstrumentation(),
                new CauDriverSocketPoolReturnInstrumentation(),
                new CauDriverQuerySocketInstrumentation(),
                new CauDriverDeleteInstrumentation());
    }
}
