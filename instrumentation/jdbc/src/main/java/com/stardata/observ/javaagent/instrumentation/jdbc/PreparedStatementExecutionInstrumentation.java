package com.stardata.observ.javaagent.instrumentation.jdbc;

import java.sql.PreparedStatement;
import java.util.Map;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import com.stardata.observ.javaagent.instrumentation.common.Constant;

import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.hasClassesNamed;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.isPublic;
import static net.bytebuddy.matcher.ElementMatchers.nameStartsWith;
import static net.bytebuddy.matcher.ElementMatchers.named;
import static net.bytebuddy.matcher.ElementMatchers.takesArguments;

public class PreparedStatementExecutionInstrumentation implements TypeInstrumentation {

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
                nameStartsWith("execute").and(takesArguments(0)).and(isPublic()),
                this.getClass().getName() + "$PreparedStatementExecutionAdvice");
    }

    public static class PreparedStatementExecutionAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void onEnter(
                @Advice.This PreparedStatement statement) {
            Span span = Java8BytecodeBridge.currentSpan();
            if (!span.getSpanContext().isValid()) {
                return;
            }

            Map<Integer, Object> arguments = PreparedStatementArgumentHelper.get(statement);

            // append the arguments as a string after the span is created
            if (arguments == null || arguments.size() == 0) {
                return;
            }
            StringBuilder builder = new StringBuilder("[");
            int index = 0;
            for (Object value : arguments.values()) {
                if (value instanceof String) {
                    builder.append(String.format("'%s'", value));
                } else {
                    builder.append(String.format("%s", value));
                }
                index++;
                if (index < arguments.size()) {
                    builder.append(",");
                }
            }
            builder.append("]");
            span.setAttribute(Constant.DB_STATEMENT_VALUES, builder.toString());
            PreparedStatementArgumentHelper.clear(statement);
        }
    }

}
