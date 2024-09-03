package com.stardata.observ.javaagent.instrumentation.springcloud;

import io.opentelemetry.javaagent.extension.instrumentation.TypeInstrumentation;
import io.opentelemetry.javaagent.extension.instrumentation.TypeTransformer;

import net.bytebuddy.asm.Advice;
import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import static net.bytebuddy.matcher.ElementMatchers.isMethod;
import static net.bytebuddy.matcher.ElementMatchers.named;

public class DiscoveryInstrumentation implements TypeInstrumentation {
    @Override
    public ElementMatcher<TypeDescription> typeMatcher() {
        return named("org.springframework.cloud.client.discovery.EnableDiscoveryClientImportSelector");
    }

    @Override
    public void transform(TypeTransformer transformer) {
        transformer.applyAdviceToMethod(
                isMethod()
                        .and(named("selectImports")),
                this.getClass().getName() + "$SelectImportsAdvice");
    }

    @SuppressWarnings("unused")
    public static class SelectImportsAdvice {

        @Advice.OnMethodEnter(suppress = Throwable.class)
        public static void trackCallDepth(
        ) {
            AppFrameworkProcessor.APP_FRAMEWORK.set(AppFrameworkProcessor.SPRING_CLOUD_APP_FRAMEWORK, "");
        }
    }
}
