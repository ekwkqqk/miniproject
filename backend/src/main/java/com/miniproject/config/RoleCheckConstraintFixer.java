package com.miniproject.config;

import org.springframework.context.annotation.Profile;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
public class RoleCheckConstraintFixer {

    private final JdbcTemplate jdbcTemplate;

    public RoleCheckConstraintFixer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void fixRoleCheckConstraint() {
        jdbcTemplate.execute("""
                DO $$
                DECLARE constraint_record RECORD;
                BEGIN
                    FOR constraint_record IN
                        SELECT con.conname
                        FROM pg_constraint con
                        JOIN pg_class rel ON rel.oid = con.conrelid
                        WHERE rel.relname = 'users'
                          AND con.contype = 'c'
                          AND pg_get_constraintdef(con.oid) LIKE '%role%'
                    LOOP
                        EXECUTE format('ALTER TABLE users DROP CONSTRAINT %I', constraint_record.conname);
                    END LOOP;
                END $$
                """);

        jdbcTemplate.execute("""
                ALTER TABLE users
                    ADD CONSTRAINT users_role_check
                    CHECK (role IN ('USER', 'SPECIAL_USER', 'SYSTEM_ADMIN'))
                """);
    }
}
