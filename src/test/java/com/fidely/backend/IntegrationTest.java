package com.fidely.backend;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTest {

    private static boolean databaseCleaned = false;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void cleanDatabaseOnce() {
        synchronized (IntegrationTest.class) {
            if (databaseCleaned) {
                return;
            }

            jdbcTemplate.execute("""
                    DO $$
                    DECLARE
                        statements TEXT;
                    BEGIN
                        SELECT string_agg(
                            format(
                                'TRUNCATE TABLE %I.%I RESTART IDENTITY CASCADE',
                                schemaname,
                                tablename
                            ),
                            '; '
                        )
                        INTO statements
                        FROM pg_tables
                        WHERE schemaname = 'public'
                          AND tablename <> 'flyway_schema_history';

                        IF statements IS NOT NULL THEN
                            EXECUTE statements;
                        END IF;
                    END
                    $$;
                    """);

            databaseCleaned = true;
        }
    }
}