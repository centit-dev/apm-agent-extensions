package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import javax.jms.Message;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.activemq.ActiveMQMessageConsumer;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class ConsumerInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.activemq.ActiveMQMessageConsumer");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                named("receive")
                        .and(takesArguments(0).or(takesArguments(1)))
                        .and(isPublic()),
                ConsumerInstrumentation.class.getName() + "$ReceiveAdvice");
        transformer.applyAdviceToMethod(
                named("receiveNoWait")
                        .and(takesArguments(0))
                        .and(isPublic()),
                ConsumerInstrumentation.class.getName() + "$ReceiveAdvice");
    }

    public static class ReceiveAdvice {
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.This ActiveMQMessageConsumer consumer,
                @Advice.Return Message message
        ) {
            if (message != null) {
                String socketAddr = SessionFieldHelper.getRemoteAddress(consumer);
                if (socketAddr == null || socketAddr.isEmpty()) {
                    return;
                }
                // socketAddr example: tcp://127.0.0.1:11211
                Context ctx = ActivemqReceiveSpanUtil.getParentContext(message);
                SessionHelper.putConsumerContext(ctx, socketAddr);
                Span parentSpan = Span.fromContext(ctx);
                if (parentSpan != null) {
                    SessionHelper.putConsumerSpanID(parentSpan.getSpanContext().getSpanId(), socketAddr);
                }
            }
        }
    }
}
