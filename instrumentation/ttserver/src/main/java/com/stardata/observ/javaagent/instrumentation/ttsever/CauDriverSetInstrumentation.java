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

public class CauDriverSetInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ai.common.cau.query.driver.CauBufferedDriver");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isPublic().
                        and(named(CauDriverValues.SET_FUNCTION_NAME)).
                        and(takesArguments(2)),
                this.getClass().getName() + "$CauDriverSetAdvice");
    }

    public static class CauDriverSetAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(0) String param,
                @Advice.Local("otelRequest") TtServerRequest request,
                @Advice.Local("otelContext") Context context,
                @Advice.Local("otelScope") Scope scope) {
            TtServerRequest socketRequest = new TtServerRequest();
            socketRequest.setKey(param);
            socketRequest.setFuncName(CauDriverValues.SET_FUNCTION_NAME);
            socketRequest.setCommand("set");
            CURRENT_REQUEST.set(socketRequest);

            Context parentContext = currentContext();

            request = new TtServerRequest();
            request.setFuncName(CauDriverValues.SET_FUNCTION_NAME);

            context = statementInstrumenter().start(parentContext, request);
            scope = context.makeCurrent();
        }

        @Advice.OnMethodExit(onThrowable = Throwable.class, suppress = Throwable.class)
        public static void onExit(
                @Advice.Thrown Throwable throwable,
                @Advice.Local("otelRequest") TtServerRequest request,
                @Advice.Local("otelContext") Context context,
                @Advice.Local("otelScope") Scope scope) {
            CURRENT_REQUEST.remove();
            if (scope != null) {
                scope.close();
                statementInstrumenter().end(context, request, null, throwable);
            }
        }
    }
}
