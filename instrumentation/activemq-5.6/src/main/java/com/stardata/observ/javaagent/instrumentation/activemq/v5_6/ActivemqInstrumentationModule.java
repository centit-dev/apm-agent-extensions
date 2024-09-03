package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;


@AutoService(InstrumentationModule.class)
public class ActivemqInstrumentationModule extends InstrumentationModule {

    public ActivemqInstrumentationModule() {
        super("activemq", "activemq-5.6");
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
                new ConsumerInstrumentation(),
                new SessionInstrumentation(),
                new ListenerInstrumentation()
        );
    }
}
