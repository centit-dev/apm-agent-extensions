package com.stardata.observ.javaagent.instrumentation.jdbc;

import java.sql.PreparedStatement;

import io.opentelemetry.javaagent.bootstrap.CallDepth;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class PreparedStatementSetterInstrumentation implements TypeInstrumentation {

    @Override
    public ElementMatcher<ClassLoader> classLoaderOptimization() {
        return hasClassesNamed("java.sql.PreparedStatement");
    }

    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return implementsInterface(named("java.sql.PreparedStatement"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                nameStartsWith("set").and(isPublic()),
                this.getClass().getName() + "$PreparedStatementSetterAdvice");
    }

    public static class PreparedStatementSetterAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.Local("otelCallDepth") CallDepth callDepth,
                @Advice.This PreparedStatement statement,
                @Advice.AllArguments(nullIfEmpty = true) Object[] args) {
            if (args == null || args.length == 0) {
                return;
            }

            // multiple delegates can be called in the same method
            // use a depth counter to avoid repeating the same work
            callDepth = CallDepth.forClass(PreparedStatement.class);
            if (callDepth.getAndIncrement() > 0) {
                return;
            }

            // record the ordered arguments
            Object index = args[0];
            if (!(index instanceof Integer)) {
                return;
            }
            PreparedStatementArgumentHelper.put(statement, (Integer) index, args[1]);
        }

        @Advice.OnMethodExit(suppress = Throwable.class)
        public static void onExit(
                @Advice.Local("otelCallDepth") CallDepth callDepth) {
            if (callDepth == null) {
                return;
            }
            callDepth.decrementAndGet();
        }
    }

}
