ALTER TABLE inscricoes
    RENAME COLUMN data_inscricao TO inscrito_em;

ALTER TABLE inscricoes
    ALTER COLUMN inscrito_em TYPE TIMESTAMP USING inscrito_em::timestamp;