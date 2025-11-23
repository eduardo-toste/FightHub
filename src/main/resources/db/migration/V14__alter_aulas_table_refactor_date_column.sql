ALTER TABLE aulas
    ALTER COLUMN data TYPE TIMESTAMP USING data::timestamp;

UPDATE aulas
SET data = date_trunc('day', COALESCE(data, now())) + INTERVAL '19 hours';