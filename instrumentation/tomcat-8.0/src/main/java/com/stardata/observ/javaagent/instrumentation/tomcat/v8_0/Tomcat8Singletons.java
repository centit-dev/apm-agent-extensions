package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

public class Tomcat8Singletons {

    private static final Tomcat8ServletBodyProvider SERVLET_BODY_PROVIDER = new Tomcat8ServletBodyProvider();

    public static Tomcat8ServletBodyProvider getServletBodyProvider() {
        return SERVLET_BODY_PROVIDER;
    }

}
