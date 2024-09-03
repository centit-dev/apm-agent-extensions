package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;


import javax.jms.MessageListener;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;

import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class ListenerInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.apache.activemq.ActiveMQMessageConsumer");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                named("setMessageListener").and(isPublic()),
                ListenerInstrumentation.class.getName() + "$MessageListenerAdvice");
    }

    @SuppressWarnings("unused")
    public static class MessageListenerAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.This ActiveMQMessageConsumer consumer,
                @Advice.Argument(value = 0, readOnly = false) MessageListener listener) {
            ActiveMQSession session = SessionFieldHelper.getSession(consumer);
            if (session != null) {
                listener = MessageAddressListener.create(listener, session);
            }
        }
    }
}
