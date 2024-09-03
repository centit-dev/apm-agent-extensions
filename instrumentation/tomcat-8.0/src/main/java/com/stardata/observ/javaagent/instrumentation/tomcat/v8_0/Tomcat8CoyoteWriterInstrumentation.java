package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.catalina.connector.CoyoteWriter;

import com.stardata.observ.javaagent.instrumentation.tomcat.common.CoyoteWriterAccessor;
import com.stardata.observ.javaagent.instrumentation.tomcat.common.StringBuilderHelper;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Tomcat8CoyoteWriterInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.connector.CoyoteWriter");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(isPublic())
                        .and(named("write")),
                this.getClass().getName() + "$TomcatCoyoteWriterAdvice");
    }

    public static class TomcatCoyoteWriterAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Local("otelCallDepth") CallDepth callDepth,
                @Advice.This CoyoteWriter writer,
                @Advice.AllArguments(nullIfEmpty = true) Object[] args) {
            if (args == null || args.length > 3) {
                return;
            }

            // write(int)
            // write(String, int, int)
            // write(char[], int, int)
            if (!(args[0] instanceof Integer) && args.length != 3) {
                return;
            }

            // multiple delegates can be called in the same method
            // use a depth counter to avoid repeating the same work
            callDepth = CallDepth.forClass(CoyoteWriter.class);
            if (callDepth.getAndIncrement() > 0) {
                return;
            }

            if (CoyoteWriterAccessor.getError(writer)) {
                return;
            }

            StringBuilder body = Tomcat8Singletons.getServletBodyProvider().getResponseBodyOrDefault(writer);
            StringBuilderHelper.append(body, true, args);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(@Advice.Local("otelCallDepth") CallDepth callDepth) {
            if (callDepth == null) {
                return;
            }
            callDepth.decrementAndGet();
        }

    }

}
