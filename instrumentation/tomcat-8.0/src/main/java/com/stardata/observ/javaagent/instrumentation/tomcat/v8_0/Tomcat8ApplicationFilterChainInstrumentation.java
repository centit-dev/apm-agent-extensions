package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import com.stardata.observ.javaagent.instrumentation.tomcat.common.TomcatApplicationFilterChainAdviceHelper;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.isPrivate;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArgument;

public class Tomcat8ApplicationFilterChainInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.catalina.core.ApplicationFilterChain");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(isPrivate())
                        .and(named("internalDoFilter"))
                        .and(takesArgument(0, named("javax.servlet.ServletRequest")))
                        .and(takesArgument(1, named("javax.servlet.ServletResponse"))),
                this.getClass().getName() + "$Tomcat8ApplicationFilterChainAdvice");
    }

    public static class Tomcat8ApplicationFilterChainAdvice {

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onMethodExit(
                @Advice.Argument(0) ServletRequest request,
                @Advice.Argument(1) ServletResponse response) {
            Span span = Java8BytecodeBridge.currentSpan();
            if (!span.getSpanContext().isValid()) {
                return;
            }

            StringBuilder body = Tomcat8Singletons.getServletBodyProvider().getRequestBody(request);
            TomcatApplicationFilterChainAdviceHelper.writeRequest(span, body);

            body = Tomcat8Singletons.getServletBodyProvider().getResponseBody(response);
            TomcatApplicationFilterChainAdviceHelper.writeResponse(span, body);
        }

    }

}
