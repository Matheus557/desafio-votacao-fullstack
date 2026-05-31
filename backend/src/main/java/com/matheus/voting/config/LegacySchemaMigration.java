package com.matheus.voting.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class LegacySchemaMigration implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public LegacySchemaMigration(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        jdbcTemplate.execute("ALTER TABLE IF EXISTS voto DROP COLUMN IF EXISTS usuario_id CASCADE");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario CASCADE");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS pauta ADD COLUMN IF NOT EXISTS status VARCHAR(255)");
        jdbcTemplate.execute("""
                UPDATE pauta
                   SET status = CASE
                       WHEN data_encerramento IS NULL THEN 'FECHADA'
                       WHEN data_encerramento > CURRENT_TIMESTAMP THEN 'ABERTA'
                       ELSE 'ENCERRADA'
                   END
                 WHERE status IS NULL
                """);
    }
}
