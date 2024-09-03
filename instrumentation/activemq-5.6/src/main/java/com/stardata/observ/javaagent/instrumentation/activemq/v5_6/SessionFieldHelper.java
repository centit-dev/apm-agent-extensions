package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import java.lang.reflect.Field;
import java.util.logging.Logger;

import org.apache.activemq.ActiveMQMessageConsumer;
import org.apache.activemq.ActiveMQSession;

import static com.stardata.observ.javaagent.instrumentation.activemq.v5_6.ActivemqConst.SESSION_FIELD_NAME;

public final class SessionFieldHelper {
    private static final Logger logger =
            Logger.getLogger(SessionFieldHelper.class.getName());

    private static final Field SESSION_FIELD = sessionContextField();

    public static String getRemoteAddress(ActiveMQMessageConsumer consumer) {
        try {
            ActiveMQSession sc = (ActiveMQSession) SESSION_FIELD.get(consumer);
            return sc.getConnection().getTransport().getRemoteAddress();
        } catch (Exception e) {
            logger.info("sessionContextField getRemoteAddress failed!");
            return null;
        }
    }

    public static ActiveMQSession getSession(ActiveMQMessageConsumer consumer) {
        try {
            ActiveMQSession sc = (ActiveMQSession) SESSION_FIELD.get(consumer);
            return sc;
        } catch (Exception e) {
            logger.info("sessionContextField getSession failed!");
            return null;
        }
    }

    private static Field sessionContextField() {
        try {
            Field field = ActiveMQMessageConsumer.class.getDeclaredField(SESSION_FIELD_NAME);
            field.setAccessible(true);
            return field;
        } catch (Exception e) {
            logger.info("sessionField set accessible failed!");
            return null;
        }
    }
}
