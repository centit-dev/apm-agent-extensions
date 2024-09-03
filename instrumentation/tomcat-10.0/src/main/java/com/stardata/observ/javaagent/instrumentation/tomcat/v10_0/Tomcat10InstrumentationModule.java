package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

import java.util.Arrays;
import java.util.List;

import com.google.auto.service.AutoService;

import io.opentelemetry.javaagent.extension.instrumentation.InstrumentationModule;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;

import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;

@AutoService(InstrumentationModule.class)
public class Tomcat10InstrumentationModule extends InstrumentationModule {

    public Tomcat10InstrumentationModule() {
        super("tomcat", "tomcat-10.0-http-body");
    }

    @Override
    public ElementMatcher.Junction<ClassLoader> classLoaderMatcher() {
        // only matches tomcat 10.0+
        return hasClassesNamed("jakarta.servlet.http.HttpServletRequest");
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
                new Tomcat10CoyoteInputStreamInstrumentation(),
                new Tomcat10CoyoteWriterInstrumentation(),
                new Tomcat10CoyoteOutputStreamInstrumentation(),
                new Tomcat10ApplicationFilterChainInstrumentation());
    }

}
