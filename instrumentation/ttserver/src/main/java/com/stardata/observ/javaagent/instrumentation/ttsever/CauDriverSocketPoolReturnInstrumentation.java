package com.stardata.observ.javaagent.instrumentation.ttsever;

import java.time.Instant;

import io.opentelemetry.context.Context;
import io.opentelemetry.instrumentation.api.internal.InstrumenterUtil;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static com.stardata.observ.javaagent.instrumentation.ttsever.CauDriverSingletons.statementInstrumenter;
import static com.stardata.observ.javaagent.instrumentation.ttsever.ValueHolder.CURRENT_REQUEST;
import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static net.bytebuddy.matcher.ElementMatchers.hasSuperType;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class CauDriverSocketPoolReturnInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return hasSuperType(named("org.apache.commons.pool.impl.GenericObjectPool"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isPublic().
                        and(named("returnObject")),
                this.getClass().getName() + "$CauDriverSocketPoolReturnAdvice");
    }

    public static class CauDriverSocketPoolReturnAdvice {
        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(@Advice.Thrown Throwable throwable) {
            TtServerRequest request = CURRENT_REQUEST.get();
            if (request != null && request.getStartTime() != null) {
                Context parentContext = currentContext();
                InstrumenterUtil.startAndEnd(
                        statementInstrumenter(),
                        parentContext,
                        request,
                        null,
                        throwable,
                        request.getStartTime(),
                        Instant.now());
            }
        }
    }
}
