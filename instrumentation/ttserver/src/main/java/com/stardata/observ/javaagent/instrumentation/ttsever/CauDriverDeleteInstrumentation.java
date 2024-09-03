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

public class CauDriverDeleteInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("com.ai.common.cau.query.driver.CauBufferedDriver");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isPublic().
                        and(named(CauDriverValues.DELETE_FUNCTION_NAME)).
                        and(takesArguments(1)),
                this.getClass().getName() + "$CauDriverDeleteAdvice");
    }

    public static class CauDriverDeleteAdvice {
        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Argument(0) String param,
                @Advice.Local("otelRequest") TtServerRequest request,
                @Advice.Local("otelContext") Context context,
                @Advice.Local("otelScope") Scope scope) {
            TtServerRequest socketRrequest = new TtServerRequest();
            socketRrequest.setKey(param);
            socketRrequest.setFuncName(CauDriverValues.DELETE_FUNCTION_NAME);
            socketRrequest.setCommand("delete");
            CURRENT_REQUEST.set(socketRrequest);

            request = new TtServerRequest();
            request.setFuncName(CauDriverValues.DELETE_FUNCTION_NAME);

            Context parentContext = currentContext();
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
