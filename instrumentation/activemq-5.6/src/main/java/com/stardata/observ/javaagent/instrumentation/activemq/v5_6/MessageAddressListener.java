package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import java.util.Objects;

import javax.jms.Message;
import javax.jms.MessageListener;

import org.apache.activemq.ActiveMQSession;

public class MessageAddressListener implements MessageListener {
    private MessageListener real;
    private ActiveMQSession session;

    MessageAddressListener(MessageListener real, ActiveMQSession session) {
        this.real = real;
        this.session = session;
    }

    public static MessageAddressListener create(MessageListener real, ActiveMQSession session) {
        return new MessageAddressListener(real, session);
    }

    @Override
    public void onMessage(Message message) {
        String parentSpanID = ActivemqReceiveSpanUtil.getSpanID(message);
        if (!Objects.equals(parentSpanID, "")) {
            String socketAddr = session.getConnection().getTransport().getRemoteAddress();
            SessionHelper.putConsumerSpanID(parentSpanID, socketAddr);
        }
        real.onMessage(message);
    }
}
