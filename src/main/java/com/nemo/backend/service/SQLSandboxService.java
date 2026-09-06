package com.nemo.backend.service;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;

@Service
public class SQLSandboxService {

    public List<Map<String, Object>> executeQuery(
            String questionId,
            String sql
    ) {

        validateQuery(sql);

        String databaseName =
                "nemo_sql_" + UUID.randomUUID()
                        .toString()
                        .replace("-", "");

        String url =
                "jdbc:h2:mem:" + databaseName
                        + ";MODE=MySQL;DB_CLOSE_DELAY=-1";

        try (Connection connection =
                     DriverManager.getConnection(
                             url,
                             "sa",
                             ""
                     )) {

            initializeDatabase(connection, questionId);

            try (PreparedStatement statement =
                         connection.prepareStatement(sql)) {

                try (ResultSet resultSet =
                             statement.executeQuery()) {

                    return convertResultSet(resultSet);
                }
            }

        } catch (SQLException exception) {

            throw new IllegalArgumentException(
                    "SQL execution failed: "
                            + exception.getMessage()
            );
        }
    }

    private void initializeDatabase(
            Connection connection,
            String questionId
    ) throws SQLException {

        if ("Q7".equalsIgnoreCase(questionId)) {

            createQ7Database(connection);

        } else if ("Q15".equalsIgnoreCase(questionId)) {

            createQ15Database(connection);

        } else {

            throw new IllegalArgumentException(
                    "SQL sandbox is not configured for "
                            + questionId
            );
        }
    }

    private void createQ7Database(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    """
                    CREATE TABLE Customers (
                        id INT PRIMARY KEY,
                        name VARCHAR(100)
                    )
                    """
            );

            statement.execute(
                    """
                    CREATE TABLE Orders (
                        id INT PRIMARY KEY,
                        customerId INT
                    )
                    """
            );

            statement.execute(
                    """
                    INSERT INTO Customers (id, name) VALUES
                    (1, 'Joe'),
                    (2, 'Henry'),
                    (3, 'Sam'),
                    (4, 'Max')
                    """
            );

            statement.execute(
                    """
                    INSERT INTO Orders (id, customerId) VALUES
                    (1, 3),
                    (2, 1)
                    """
            );
        }
    }

    private void createQ15Database(
            Connection connection
    ) throws SQLException {

        try (Statement statement =
                     connection.createStatement()) {

            statement.execute(
                    """
                    CREATE TABLE Tree (
                        id INT PRIMARY KEY,
                        p_id INT
                    )
                    """
            );

            statement.execute(
                    """
                    INSERT INTO Tree (id, p_id) VALUES
                    (1, NULL),
                    (2, 1),
                    (3, 1),
                    (4, 2),
                    (5, 2)
                    """
            );
        }
    }

    private void validateQuery(String sql) {

        if (sql == null || sql.isBlank()) {

            throw new IllegalArgumentException(
                    "SQL query cannot be empty"
            );
        }

        String normalized =
                sql.trim()
                        .toLowerCase(Locale.ROOT);

        if (!normalized.startsWith("select")) {

            throw new IllegalArgumentException(
                    "Only SELECT queries are allowed"
            );
        }

        String withoutTrailingSemicolon =
                normalized.endsWith(";")
                        ? normalized.substring(
                                0,
                                normalized.length() - 1
                        ).trim()
                        : normalized;

        if (withoutTrailingSemicolon.contains(";")) {

            throw new IllegalArgumentException(
                    "Multiple SQL statements are not allowed"
            );
        }

        String[] forbidden = {
                "insert ",
                "update ",
                "delete ",
                "drop ",
                "alter ",
                "create ",
                "truncate ",
                "merge ",
                "grant ",
                "revoke ",
                "call ",
                "exec ",
                "execute ",
                "into "
        };

        for (String keyword : forbidden) {

            if (normalized.contains(keyword)) {

                throw new IllegalArgumentException(
                        "This SQL operation is not allowed"
                );
            }
        }
    }

    private List<Map<String, Object>> convertResultSet(
            ResultSet resultSet
    ) throws SQLException {

        List<Map<String, Object>> rows =
                new ArrayList<>();

        ResultSetMetaData metadata =
                resultSet.getMetaData();

        int columnCount =
                metadata.getColumnCount();

        while (resultSet.next()) {

            Map<String, Object> row =
                    new LinkedHashMap<>();

            for (int i = 1; i <= columnCount; i++) {

                String columnName =
                        metadata.getColumnLabel(i);

                row.put(
                        columnName,
                        resultSet.getObject(i)
                );
            }

            rows.add(row);
        }

        return rows;
    }
}