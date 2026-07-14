package com.miniproject.config;

import liquibase.integration.spring.SpringLiquibase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

/**
 * Liquibase {@code databasechangeloglock} 중복 row로 인한 기동 실패를 방지한다.
 * SpringLiquibase 초기화(afterPropertiesSet) 직전에 락 테이블을 1행으로 정리한다.
 */
@Component
@ConditionalOnProperty(name = "spring.liquibase.enabled", havingValue = "true", matchIfMissing = true)
public class LiquibaseLockCleanupProcessor implements BeanPostProcessor {

    private static final Logger log = LoggerFactory.getLogger(LiquibaseLockCleanupProcessor.class);

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof SpringLiquibase liquibase) {
            DataSource dataSource = liquibase.getDataSource();
            if (dataSource != null) {
                repairLockTable(dataSource);
            }
        }
        return bean;
    }

    private void repairLockTable(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            boolean exists;
            try (ResultSet rs = statement.executeQuery("""
                    SELECT EXISTS (
                      SELECT 1
                      FROM information_schema.tables
                      WHERE table_schema = 'public'
                        AND table_name = 'databasechangeloglock'
                    )
                    """)) {
                rs.next();
                exists = rs.getBoolean(1);
            }
            if (!exists) {
                return;
            }

            int deleted = statement.executeUpdate("""
                    DELETE FROM databasechangeloglock a
                    USING databasechangeloglock b
                    WHERE a.ctid < b.ctid
                      AND a.id = b.id
                    """);
            statement.executeUpdate("""
                    UPDATE databasechangeloglock
                    SET locked = FALSE,
                        lockgranted = NULL,
                        lockedby = NULL
                    WHERE id = 1
                    """);
            statement.executeUpdate("""
                    INSERT INTO databasechangeloglock (id, locked)
                    SELECT 1, FALSE
                    WHERE NOT EXISTS (
                      SELECT 1 FROM databasechangeloglock WHERE id = 1
                    )
                    """);
            if (deleted > 0) {
                log.warn("Repaired Liquibase lock table (removed {} duplicate row(s))", deleted);
            }
        } catch (Exception ex) {
            log.warn("Failed to repair Liquibase lock table: {}", ex.getMessage());
        }
    }
}
