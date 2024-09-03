package com.stardata.observ.javaagent.instrumentation.activemq.v5_6;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import io.opentelemetry.instrumentation.testing.internal.AutoCleanupExtension;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.sdk.testing.assertj.AttributeAssertion;
import io.opentelemetry.semconv.incubating.MessagingIncubatingAttributes;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.assertj.core.api.AbstractAssert;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.opentelemetry.api.trace.SpanKind.CONSUMER;
import static io.opentelemetry.api.trace.SpanKind.PRODUCER;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.equalTo;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.satisfies;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

@Disabled
public class AbstractJms1Test {
    static final Logger logger = LoggerFactory.getLogger(AbstractJms1Test.class);

    @RegisterExtension
    static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

    @RegisterExtension
    static final AutoCleanupExtension cleanup = AutoCleanupExtension.create();

    static ActiveMQConnectionFactory connectionFactory;
    static Connection connection;
    static Session session;

    @BeforeAll
    static void setUp() throws JMSException {
        connectionFactory =
                new ActiveMQConnectionFactory(
                        "tcp://127.0.0.1:61616");
        Connection connection = connectionFactory.createConnection();
        connection.start();
        session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
    }

    @AfterAll
    static void tearDown() throws JMSException {
        if (session != null) {
            session.close();
        }
        if (connection != null) {
            connection.close();
        }
    }

    @ArgumentsSource(DestinationsProvider.class)
    @ParameterizedTest
    void testMessageConsumer(
            DestinationFactory destinationFactory, String destinationName, boolean isTemporary)
            throws JMSException {

        // given
        Destination destination = destinationFactory.create(session);
        TextMessage sentMessage = session.createTextMessage("a message");

        MessageProducer producer = session.createProducer(destination);
        cleanup.deferCleanup(producer::close);
        MessageConsumer consumer = session.createConsumer(destination);
        cleanup.deferCleanup(consumer::close);

        // when
        testing.runWithSpan("producer parent", () -> producer.send(sentMessage));

        TextMessage receivedMessage =
                testing.runWithSpan("consumer parent", () -> (TextMessage) consumer.receive());

        // then
        assertThat(receivedMessage.getText()).isEqualTo(sentMessage.getText());

        String messageId = receivedMessage.getJMSMessageID();

        testing.waitAndAssertTraces(
                trace -> {
                    trace.hasSpansSatisfyingExactly(
                            span -> span.hasName("producer parent").hasNoParent(),
                            span ->
                                    span.hasName(destinationName + " publish")
                                            .hasKind(PRODUCER)
                                            .hasParent(trace.getSpan(0))
                                            .hasAttributesSatisfyingExactly(
                                                    equalTo(MessagingIncubatingAttributes.MESSAGING_SYSTEM, "jms"),
                                                    equalTo(
                                                            MessagingIncubatingAttributes.MESSAGING_DESTINATION_NAME,
                                                            destinationName),
                                                    equalTo(MessagingIncubatingAttributes.MESSAGING_OPERATION, "publish"),
                                                    equalTo(MessagingIncubatingAttributes.MESSAGING_MESSAGE_ID, messageId),
                                                    messagingTempDestination(isTemporary)));

                },
                trace ->
                        trace.hasSpansSatisfyingExactly(
                                span -> span.hasName("consumer parent").hasNoParent(),
                                span ->
                                        span.hasName(destinationName + " receive")
                                                .hasKind(CONSUMER)
                                                .hasParent(trace.getSpan(0))
                                                .hasAttributesSatisfyingExactly(
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_SYSTEM, "jms"),
                                                        equalTo(
                                                                MessagingIncubatingAttributes.MESSAGING_DESTINATION_NAME,
                                                                destinationName),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_OPERATION, "receive"),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_MESSAGE_ID, messageId),
                                                        messagingTempDestination(isTemporary))));
    }

    @ArgumentsSource(DestinationsProvider.class)
    @ParameterizedTest
    void testMessageListener(
            DestinationFactory destinationFactory, String destinationName, boolean isTemporary)
            throws Exception {

        // given
        Destination destination = destinationFactory.create(session);
        TextMessage sentMessage = session.createTextMessage("a message");

        MessageProducer producer = session.createProducer(null);
        cleanup.deferCleanup(producer::close);
        MessageConsumer consumer = session.createConsumer(destination);
        cleanup.deferCleanup(consumer::close);

        CompletableFuture<TextMessage> receivedMessageFuture = new CompletableFuture<>();
        consumer.setMessageListener(
                message ->
                        testing.runWithSpan(
                                "consumer", () -> receivedMessageFuture.complete((TextMessage) message)));

        // when
        testing.runWithSpan("producer parent", () -> producer.send(destination, sentMessage));

        // then
        TextMessage receivedMessage = receivedMessageFuture.get(10, TimeUnit.SECONDS);
        assertThat(receivedMessage.getText()).isEqualTo(sentMessage.getText());

        String messageId = receivedMessage.getJMSMessageID();

        testing.waitAndAssertTraces(
                trace ->
                        trace.hasSpansSatisfyingExactly(
                                span -> span.hasName("producer parent").hasNoParent(),
                                span ->
                                        span.hasName(destinationName + " publish")
                                                .hasKind(PRODUCER)
                                                .hasParent(trace.getSpan(0))
                                                .hasAttributesSatisfyingExactly(
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_SYSTEM, "jms"),
                                                        equalTo(
                                                                MessagingIncubatingAttributes.MESSAGING_DESTINATION_NAME,
                                                                destinationName),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_OPERATION, "publish"),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_MESSAGE_ID, messageId),
                                                        messagingTempDestination(isTemporary)),
                                span ->
                                        span.hasName(destinationName + " process")
                                                .hasKind(CONSUMER)
                                                .hasParent(trace.getSpan(1))
                                                .hasAttributesSatisfyingExactly(
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_SYSTEM, "jms"),
                                                        equalTo(
                                                                MessagingIncubatingAttributes.MESSAGING_DESTINATION_NAME,
                                                                destinationName),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_OPERATION, "process"),
                                                        equalTo(MessagingIncubatingAttributes.MESSAGING_MESSAGE_ID, messageId),
                                                        messagingTempDestination(isTemporary)),
                                span -> span.hasName("consumer").hasParent(trace.getSpan(2))));
    }

    static AttributeAssertion messagingTempDestination(boolean isTemporary) {
        return isTemporary
                ? equalTo(MessagingIncubatingAttributes.MESSAGING_DESTINATION_TEMPORARY, true)
                : satisfies(
                MessagingIncubatingAttributes.MESSAGING_DESTINATION_TEMPORARY, AbstractAssert::isNull);
    }

    static final class DestinationsProvider implements ArgumentsProvider {

        @Override
        public Stream<? extends Arguments> provideArguments(ExtensionContext context) {
            DestinationFactory topic = session -> session.createTopic("someTopic");
            DestinationFactory queue = session -> session.createQueue("someQueue");
            DestinationFactory tempTopic = Session::createTemporaryTopic;
            DestinationFactory tempQueue = Session::createTemporaryQueue;

            return Stream.of(
                    //arguments(topic, "someTopic", false),
                    arguments(queue, "someQueue", false));
            //arguments(tempTopic, "(temporary)", true),
            //arguments(tempQueue, "(temporary)", true));
        }
    }

    @FunctionalInterface
    interface DestinationFactory {

        Destination create(Session session) throws JMSException;
    }

    @FunctionalInterface
    interface MessageReceiver {

        Message receive(MessageConsumer consumer) throws JMSException;
    }
}
