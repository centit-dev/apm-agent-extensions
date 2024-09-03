package com.stardata.observ.javaagent.instrumentation.spymemcached;

import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import static java.util.Collections.singletonList;

@AutoService(InstrumentationModule.class)
public class SpymemcachedInstrumentationModule extends InstrumentationModule {

    public SpymemcachedInstrumentationModule() {
        super("spymemcached", "spymemcached-nodeaddress");
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("com.stardata.observ.javaagent.instrumentation");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new OperationInstrumentation());
    }
}
