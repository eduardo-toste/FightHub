-- Removing columns
ALTER TABLE aulas
DROP COLUMN data_hora,
DROP COLUMN professor_id,
DROP COLUMN status;

-- Adding columns
ALTER TABLE aulas
ADD COLUMN titulo VARCHAR(100) NOT NULL,
ADD COLUMN descricao VARCHAR(200),
ADD COLUMN data DATE NOT NULL,
ADD COLUMN limite_alunos INT NOT NULL,
ADD COLUMN ativo BOOLEAN NOT NULL;