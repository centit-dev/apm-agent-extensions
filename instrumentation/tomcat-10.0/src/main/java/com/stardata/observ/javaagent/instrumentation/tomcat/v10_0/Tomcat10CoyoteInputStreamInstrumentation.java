package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

import java.nio.ByteBuffer;

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

public class Tomcat10CoyoteInputStreamInstrumentation implements TypeInstrumentation {

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
                this.getClass().getName() + "$Tomcat10CoyoteInputStreamAdvice");
    }

    public static class Tomcat10CoyoteInputStreamAdvice {

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.This CoyoteInputStream inputStream,
                @Advice.Return int result,
                @Advice.AllArguments(nullIfEmpty = true) Object[] args) {
            if (args == null) {
                StringBuilder body = Tomcat10Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, result);
                return;
            }
            if (args.length == 1 && args[0] instanceof ByteBuffer) {
                ByteBuffer bytes = (ByteBuffer) args[0];
                StringBuilder body = Tomcat10Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, bytes);
            } else if (args.length == 3) {
                byte[] bytes = (byte[]) args[0];
                StringBuilder body = Tomcat10Singletons.getServletBodyProvider().getRequestBodyOrDefault(inputStream);
                StringBuilderHelper.append(body, bytes, (int) args[1], result);
            }
        }

    }

}
