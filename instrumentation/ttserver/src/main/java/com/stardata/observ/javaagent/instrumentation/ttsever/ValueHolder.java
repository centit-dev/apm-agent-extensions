package com.stardata.observ.javaagent.instrumentation.ttsever;

public final class ValueHolder {

    public static final ThreadLocal<TtServerRequest> CURRENT_REQUEST = new ThreadLocal<>();

    private ValueHolder() {
    }
}
