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
        jdbcTemplate.execute("ALTER TABLE IF EXISTS voto ADD COLUMN IF NOT EXISTS associate_id BIGINT");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS voto DROP CONSTRAINT IF EXISTS uke6g3jdc6xj1dieakfaaf7uiow");
        jdbcTemplate.execute("ALTER TABLE IF EXISTS voto DROP CONSTRAINT IF EXISTS voto_voto_check");
        jdbcTemplate.execute("UPDATE voto SET voto = 'YES' WHERE voto = 'SIM'");
        jdbcTemplate.execute("UPDATE voto SET voto = 'NO' WHERE voto = 'NAO'");
        jdbcTemplate.execute("""
                DO $$
                BEGIN
                    IF EXISTS (
                        SELECT 1
                          FROM information_schema.tables
                         WHERE table_schema = 'public'
                           AND table_name = 'voto'
                    )
                    AND NOT EXISTS (
                        SELECT 1
                          FROM pg_constraint
                         WHERE conname = 'voto_voto_check'
                    ) THEN
                        ALTER TABLE voto
                            ADD CONSTRAINT voto_voto_check
                            CHECK (voto IN ('YES', 'NO'));
                    END IF;
                END $$;
                """);
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
