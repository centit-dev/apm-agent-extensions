package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import io.opentelemetry.api.trace.Span;

import com.stardata.observ.javaagent.instrumentation.common.Constant;

public class TomcatApplicationFilterChainAdviceHelper {

    public static void writeRequest(Span span, StringBuilder body) {
        if (body == null || body.length() == 0) {
            return;
        }
        span.setAttribute(Constant.HTTP_REQUEST_BODY, body.toString());
        body.setLength(0);
    }

    public static void writeResponse(Span span, StringBuilder body) {
        if (body == null || body.length() == 0) {
            return;
        }
        span.setAttribute(Constant.HTTP_RESPONSE_BODY, body.toString());
        body.setLength(0);
    }

}
