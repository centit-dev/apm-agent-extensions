package com.stardata.observ.javaagent.instrumentation.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.instrumentation.testing.junit.AgentInstrumentationExtension;
import io.opentelemetry.instrumentation.testing.junit.InstrumentationExtension;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions;
import io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.StringAssertConsumer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.stardata.observ.javaagent.instrumentation.common.Constant;

import static java.util.concurrent.ThreadLocalRandom.current;

public class PreparedStatementAdviceTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(PreparedStatementAdviceTest.class);
    private static Connection connection;

    @RegisterExtension
    private static final InstrumentationExtension testing = AgentInstrumentationExtension.create();

    @BeforeAll
    static void setUp() throws SQLException, ClassNotFoundException {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:db1;MODE=MYSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1", "sa", "");
        connection.prepareStatement("CREATE TABLE users (id INT PRIMARY KEY, name VARCHAR(255))").execute();
    }

    @AfterAll
    static void tearDown() throws SQLException {
        connection.close();
    }

    @Test
    void testOnEnter() {
        // send the first query
        testing.runWithSpan("first-query", () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ? OR id = ? OR name = ?");
                statement.setInt(1, current().nextInt(100_000, 1_000_000));
                statement.setInt(2, current().nextInt(100_000, 1_000_000));
                statement.setString(3, "John Doe");
                statement.execute();
            } catch (SQLException ex) {
                LOGGER.error("Failed to execute query", ex);
                return;
            }
        });

        // send the second query
        testing.runWithSpan("second-query", () -> {
            try {
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM users WHERE id = ? OR id = ? OR name = ?");
                statement.setInt(1, current().nextInt(100_000, 1_000_000));
                statement.setInt(2, current().nextInt(100_000, 1_000_000));
                statement.setString(3, "Jane Doe");
                statement.execute();
            } catch (SQLException ex) {
                LOGGER.error("Failed to execute query", ex);
                return;
            }
        });

        testing.waitAndAssertTraces(
            trace -> {
                AttributeKey<String> key = AttributeKey.stringKey(Constant.DB_STATEMENT_VALUES);
                StringAssertConsumer assertion = value -> value.contains("John Doe");
                trace.singleElement().hasAttribute(OpenTelemetryAssertions.satisfies(key, assertion));
            },
            trace -> {
                AttributeKey<String> key = AttributeKey.stringKey(Constant.DB_STATEMENT_VALUES);
                StringAssertConsumer assertion = value -> value.contains("Jane Doe");
                trace.singleElement().hasAttribute(OpenTelemetryAssertions.satisfies(key, assertion));
            }
        );
    }

}
