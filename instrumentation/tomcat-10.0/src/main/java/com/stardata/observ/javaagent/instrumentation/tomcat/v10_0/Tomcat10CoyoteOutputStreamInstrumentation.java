package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.catalina.connector.CoyoteOutputStream;

import com.stardata.observ.javaagent.instrumentation.tomcat.common.StringBuilderHelper;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Tomcat10CoyoteOutputStreamInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.connector.CoyoteOutputStream");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(isPublic())
                        .and(named("write")),
                this.getClass().getName() + "$Tomcat10CoyoteOutputStreamAdvice");
    }

    public static class Tomcat10CoyoteOutputStreamAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Local("otelCallDepth") CallDepth callDepth,
                @Advice.This CoyoteOutputStream writer,
                @Advice.AllArguments(nullIfEmpty = true) Object[] args) {
            if (args == null || args.length > 3) {
                return;
            }

            // write(int)
            // write(byte[], int, int)
            // write(ByteBuffer)
            if (args[0] instanceof byte[] && args.length == 1) {
                // skip write(byte[])
                return;
            }

            // multiple delegates can be called in the same method
            // use a depth counter to avoid repeating the same work
            callDepth = CallDepth.forClass(CoyoteOutputStream.class);
            if (callDepth.getAndIncrement() > 0) {
                return;
            }

            StringBuilder body = Tomcat10Singletons.getServletBodyProvider().getResponseBodyOrDefault(writer);
            StringBuilderHelper.append(body, false, args);
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
