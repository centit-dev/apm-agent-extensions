package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.activemq.ActiveMQSession;

import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;


public class SessionInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.activemq.ActiveMQSession");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(named("send")),
                this.getClass().getName() + "$SendAdvice");
    }

    public static class SendAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void OnEnter(
                @Advice.This ActiveMQSession session
        ) {
            Context ctx = currentContext();
            Span span = Span.fromContext(ctx);
            if (span != null && SessionHelper.getProducerInfo(span.getSpanContext().getSpanId()) != null) {
                String socketAddr = session.getConnection().getTransport().getRemoteAddress();
                // socketAddr example: tcp://127.0.0.1:11211
                span.setAttribute(ActivemqConst.ACTIVEMQ_ADDRESS, socketAddr);
            }
        }
    }
}
