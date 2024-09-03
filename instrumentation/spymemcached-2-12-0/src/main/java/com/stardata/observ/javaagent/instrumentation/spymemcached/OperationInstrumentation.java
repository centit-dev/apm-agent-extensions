package com.stardata.observ.javaagent.instrumentation.spymemcached;

import io.opentelemetry.context.Context;
import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;
import net.spy.memcached.MemcachedNode;

import static io.opentelemetry.javaagent.bootstrap.Java8BytecodeBridge.currentContext;
import static io.opentelemetry.javaagent.extension.matcher.AgentElementMatchers.implementsInterface;
import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class OperationInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return implementsInterface(named("net.spy.memcached.ops.Operation"));
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(named("setHandlingNode")),
                this.getClass().getName() + "$SetHandingNodeAdvice");
    }

    @SuppressWarnings("unused")
    public static class SetHandingNodeAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void trackCallDepth(
                @Advice.Argument(0) MemcachedNode v1
        ) {
            Context ctx = currentContext();
            String socketAddr = v1.getSocketAddress().toString();
            // socketAddr example: localhost/127.0.0.1:11211
            String nodeAddr = socketAddr.split("/").length >= 2 ? socketAddr.split("/")[1] : socketAddr;
            OperationFutureHelper.put(ctx, nodeAddr);
        }
    }
}
