package com.stardata.observ.javaagent.instrumentation.ttsever;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

import static com.stardata.observ.javaagent.instrumentation.ttsever.CauDriverSingletons.statementInstrumenter;
import static com.stardata.observ.javaagent.instrumentation.ttsever.ValueHolder.CURRENT_REQUEST;

public class CauDriverQueryInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ai.common.cau.query.driver.CauBufferedDriver");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isPublic().
                        and(named("get")).
                        and(takesArguments(1)),
                this.getClass().getName() + "$CauDriverQueryAdvice");
    }

    public static class CauDriverQueryAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(0) Object param,
                @Advice.Local("otelRequest") TtServerRequest request,
                @Advice.Local("otelContext") Context context,
                @Advice.Local("otelScope") Scope scope) {
            Context parentContext = currentContext();
            request = new TtServerRequest();

            if (param instanceof String) {
                request.setCommand("get");
                request.setFuncName("get");
                request.setKey((String) param);
            } else if (param instanceof String[]) {
                request.setCommand("batchget");
                request.setFuncName("batchget");
                request.setKeys((String[]) param);
            }

            context = statementInstrumenter().start(parentContext, request);
            CURRENT_REQUEST.set(request);
            scope = context.makeCurrent();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Thrown Throwable throwable,
                @Advice.Local("otelRequest") TtServerRequest request,
                @Advice.Local("otelContext") Context context,
                @Advice.Local("otelScope") Scope scope) {
            if (scope != null) {
                scope.close();
                CURRENT_REQUEST.remove();
                statementInstrumenter().end(context, request, null, throwable);
            }
        }
    }
}
