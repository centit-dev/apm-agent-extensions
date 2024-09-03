package com.stardata.observ.javaagent.instrumentation.tomcat.v8_0;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;

import javax.servlet.ServletException;

import org.apache.catalina.startup.Tomcat;

class TomcatServer {

    private final Tomcat tomcat;

    public TomcatServer() {
        tomcat = new Tomcat();
        init();
    }

    public String getAddress() {
        return String.format("%s:%d", tomcat.getServer().getAddress(), tomcat.getConnector().getPort());
    }

    public void start() {
        try {
            tomcat.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        try {
            tomcat.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void init() {
        tomcat.setBaseDir("/tmp/tomcat");
        try (ServerSocket socket = new ServerSocket(0)) {
            tomcat.setPort(socket.getLocalPort());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            String baseDir = getClass().getResource("/webapp").getPath();
            baseDir = new File(baseDir).getAbsolutePath();
            tomcat.addWebapp("", baseDir);
        } catch (ServletException ignored) {
            // ignored
        }

    }

}
