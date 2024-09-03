package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

import java.io.PrintWriter;

import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import io.opentelemetry.instrumentation.api.util.VirtualField;

import org.apache.catalina.connector.CoyoteInputStream;
import org.apache.catalina.connector.CoyoteOutputStream;
import org.apache.catalina.connector.CoyoteWriter;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.RequestFacade;
import org.apache.catalina.connector.Response;
import org.apache.catalina.connector.ResponseFacade;

import com.stardata.observ.javaagent.instrumentation.tomcat.common.RequestAccessor;
import com.stardata.observ.javaagent.instrumentation.tomcat.common.ResponseAccessor;
import com.stardata.observ.javaagent.instrumentation.tomcat.common.ServletBodyProvider;

public class Tomcat8ServletBodyProvider implements ServletBodyProvider<ServletRequest, ServletResponse> {

    private static final VirtualField<ServletInputStream, StringBuilder> REQUEST_BODY =
            VirtualField.find(ServletInputStream.class, StringBuilder.class);
    private static final VirtualField<ServletOutputStream, StringBuilder> RESPONSE_STREAM_BODY =
            VirtualField.find(ServletOutputStream.class, StringBuilder.class);
    private static final VirtualField<PrintWriter, StringBuilder> RESPONSE_WRITER_BODY =
            VirtualField.find(PrintWriter.class, StringBuilder.class);

    @Override
    public StringBuilder getRequestBody(ServletRequest request) {
        ServletInputStream inputStream = getInputStream(request);
        if (inputStream == null) {
            return null;
        }
        return REQUEST_BODY.get(inputStream);
    }

    private ServletInputStream getInputStream(ServletRequest request) {
        try {
            if (request instanceof RequestFacade) {
                RequestFacade req = (RequestFacade) request;
                if (RequestAccessor.usingInputStream(req)) {
                    return req.getInputStream();
                }
            } else if (request instanceof Request) {
                Request req = (Request) request;
                if (RequestAccessor.usingInputStream(req)) {
                    return req.getInputStream();
                }
            }
        } catch (Exception ignored) {
            // ignored
        }
        return null;
    }

    @Override
    public StringBuilder getResponseBody(ServletResponse response) {
        boolean usingInputStream;
        if (response instanceof ResponseFacade) {
            usingInputStream = ResponseAccessor.usingOutputStream((ResponseFacade) response);
        } else if (response instanceof Response) {
            usingInputStream = ResponseAccessor.usingOutputStream((Response) response);
        } else {
            return null;
        }

        try {
            if (usingInputStream) {
                ServletOutputStream outputStream = response.getOutputStream();
                return RESPONSE_STREAM_BODY.get(outputStream);
            } else {
                PrintWriter writer = response.getWriter();
                return RESPONSE_WRITER_BODY.get(writer);
            }
        } catch (Exception ignored) {
            // ignored
        }

        return null;
    }

    @Override
    public StringBuilder getRequestBodyOrDefault(CoyoteInputStream inputStream) {
        StringBuilder body = REQUEST_BODY.get(inputStream);
        if (body == null) {
            body = new StringBuilder();
            REQUEST_BODY.set(inputStream, body);
        }
        return body;
    }

    @Override
    public StringBuilder getResponseBodyOrDefault(CoyoteOutputStream outputStream) {
        StringBuilder body = RESPONSE_STREAM_BODY.get(outputStream);
        if (body == null) {
            body = new StringBuilder();
            RESPONSE_STREAM_BODY.set(outputStream, body);
        }
        return body;
    }

    @Override
    public StringBuilder getResponseBodyOrDefault(CoyoteWriter writer) {
        StringBuilder body = RESPONSE_WRITER_BODY.get(writer);
        if (body == null) {
            body = new StringBuilder();
            RESPONSE_WRITER_BODY.set(writer, body);
        }
        return body;
    }

}
