package com.stardata.observ.javaagent.instrumentation.baggage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.StringJoiner;

class SimpleClient {

    private final String address;

    public SimpleClient(String address) {
        this.address = address;
    }

    public String request(String method, String path, Map<String, String> headers) {
        URL url;
        try {
            url = new URL(String.format("http://%s%s", address, path));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        HttpURLConnection connection;
        try {
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod(method);
            headers.forEach((key, value) -> connection.setRequestProperty(key, value));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
            StringJoiner joiner = new StringJoiner("\n");
            String line;
            while ((line = reader.readLine()) != null) {
                joiner.add(line);
            }
            return joiner.toString();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
