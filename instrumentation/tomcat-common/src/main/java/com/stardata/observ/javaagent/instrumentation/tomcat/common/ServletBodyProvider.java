package com.stardata.observ.javaagent.instrumentation.tomcat.common;

import org.apache.catalina.connector.CoyoteInputStream;
import org.apache.catalina.connector.CoyoteOutputStream;
import org.apache.catalina.connector.CoyoteWriter;

public interface ServletBodyProvider<REQUEST, RESPONSE> {

    StringBuilder getRequestBody(REQUEST request);

    StringBuilder getResponseBody(RESPONSE response);

    StringBuilder getRequestBodyOrDefault(CoyoteInputStream inputStream);

    StringBuilder getResponseBodyOrDefault(CoyoteOutputStream writer);

    StringBuilder getResponseBodyOrDefault(CoyoteWriter writer);

}
