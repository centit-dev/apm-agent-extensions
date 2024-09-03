package com.stardata.observ.javaagent.instrumentation.springcloud;

import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import static java.util.Collections.singletonList;

@AutoService(InstrumentationModule.class)
public class SpringCloudInstrumentationModule extends InstrumentationModule {
    public SpringCloudInstrumentationModule() {
        super("springcloud", "protocal");
    }

    @Override
    public boolean isHelperClass(String className) {
        return className.startsWith("com.stardata.observ.javaagent.instrumentation");
    }

    @Override
    public List<TypeInstrumentation> typeInstrumentations() {
        return singletonList(new DiscoveryInstrumentation());
    }
}
