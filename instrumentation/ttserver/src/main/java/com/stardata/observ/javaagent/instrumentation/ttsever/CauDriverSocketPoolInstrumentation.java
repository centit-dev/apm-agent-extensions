package com.stardata.observ.javaagent.instrumentation.ttsever;

import java.net.Socket;
import java.time.Instant;
import java.util.Objects;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

import static com.stardata.observ.javaagent.instrumentation.ttsever.ValueHolder.CURRENT_REQUEST;

public class CauDriverSocketPoolInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("org.apache.commons.pool.impl.GenericObjectPool"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isPublic().
                        and(named("borrowObject")),
                this.getClass().getName() + "$CauDriverSocketPoolBorrowAdvice");
    }

    public static class CauDriverSocketPoolBorrowAdvice {
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Return Object socket) {
            Socket socketObj = (Socket) socket;

            TtServerRequest request = CURRENT_REQUEST.get();
            if (request == null) {
                return;
            }
            request.setHost(socketObj.getInetAddress().getHostName());
            request.setPort((long) socketObj.getPort());

            if (Objects.equals(request.getFuncName(), CauDriverValues.SET_FUNCTION_NAME) ||
                    Objects.equals(request.getFuncName(), CauDriverValues.DELETE_FUNCTION_NAME)) {
                request.setFuncName(request.getFuncName() + ".socket");
                request.setStartTime(Instant.now());
            }
        }
    }
}
