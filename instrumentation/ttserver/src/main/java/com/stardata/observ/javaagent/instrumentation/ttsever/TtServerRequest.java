package com.stardata.observ.javaagent.instrumentation.ttsever;

import java.time.Instant;
import java.util.Objects;

public class TtServerRequest {
    private String funcName;
    private String host;
    private Long port;
    private String command;
    private String key;
    private String[] keys;
    private Instant startTime;

    public void setStartTime(Instant startTime) {
        this.startTime = startTime;
    }

    public Instant getStartTime() {
        return startTime;
    }

    public TtServerRequest() {
    }

    public String getCommand() {
        return command;
    }

    public String getFuncName() {
        return funcName;
    }

    public String getHost() {
        return host;
    }

    public String getKey() {
        return key;
    }

    public Long getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    public String[] getKeys() {
        return keys;
    }

    public String getConnetionUrl() {
        if (this.host == null) {
            return null;
        } else {
            return this.host + ":" + this.port;
        }
    }

    public String getSystem() {
        if (Objects.equals(this.funcName, CauDriverValues.SET_FUNCTION_NAME) ||
                Objects.equals(this.funcName, CauDriverValues.DELETE_FUNCTION_NAME)) {
            return null;
        } else {
            return "ttserver";
        }
    }

    public void setKeys(String[] keys) {
        if (keys.length > 10) {
            this.keys = new String[10];
            System.arraycopy(keys, 0, this.keys, 0, 10);
        } else {
            this.keys = keys;
        }
    }

    public void setKey(String key) {
        this.key = key;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public void setFuncName(String funcName) {
        this.funcName = funcName;
    }

    public String getSpanKeyAttr() {
        if (Objects.equals(this.getKey(), "")) {
            return String.join(",", this.keys);
        } else {
            return this.getKey();
        }
    }
}
