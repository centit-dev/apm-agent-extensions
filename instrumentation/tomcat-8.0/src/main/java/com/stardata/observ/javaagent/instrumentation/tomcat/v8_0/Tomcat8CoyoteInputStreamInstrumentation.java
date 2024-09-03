package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.catalina.connector.CoyoteInputStream;

import com.stardata.observ.javaagent.instrumentation.tomcat.common.StringBuilderHelper;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class Tomcat8CoyoteInputStreamInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.connector.CoyoteInputStream");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(isPublic())
                        .and(named("read")),
                this.getClass().getName() + "$Tomcat8CoyoteInputStreamAdvice");
    }

    public static class Tomcat8CoyoteInputStreamAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.This CoyoteInputStream inputStream,
                @Advice.Return int result,
                @Advice.AllArguments(nullIfEmpty = true) Object[] args) {
            if (args == null) {
                StringBuilder body = Tomcat8Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, result);
                return;
            }
            if (args.length == 1) {
                byte[] bytes = (byte[]) args[0];
                StringBuilder body = Tomcat8Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, bytes, 0, result);
            } else if (args.length == 3) {
                byte[] bytes = (byte[]) args[0];
                StringBuilder body = Tomcat8Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, bytes, (int) args[1], result);
            }
        }

    }

}
