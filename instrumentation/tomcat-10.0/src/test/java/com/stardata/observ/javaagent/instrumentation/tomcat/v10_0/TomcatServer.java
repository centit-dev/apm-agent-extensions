package com.stardata.observ.javaagent.instrumentation.tomcat.v10_0;

import java.io.IOException;
import java.net.ServerSocket;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.catalina.Context;
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

        Context context = tomcat.addContext("", null);
        Tomcat.addServlet(context, "hello", new HttpServlet() {

            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
                resp.getWriter().write("Hello, World!");
            }

            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp)
                    throws ServletException, IOException {
                byte[] body = new byte[req.getContentLength()];
                req.getInputStream().read(body);
                resp.getOutputStream().write(body);
            }

        });

        context.addServletMappingDecoded("/", "hello");
    }

}
