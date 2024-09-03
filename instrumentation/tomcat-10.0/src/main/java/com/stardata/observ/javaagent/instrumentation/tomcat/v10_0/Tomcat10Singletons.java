package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

public class Tomcat10Singletons {

    private static final Tomcat10ServletBodyProvider SERVLET_BODY_PROVIDER = new Tomcat10ServletBodyProvider();

    public static Tomcat10ServletBodyProvider getServletBodyProvider() {
        return SERVLET_BODY_PROVIDER;
    }

}
