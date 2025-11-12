-- Removing columns
ALTER TABLE presencas
DROP COLUMN aluno_id,
DROP COLUMN aula_id;

-- Adding columns
ALTER TABLE presencas
ADD COLUMN inscricao_id UUID NOT NULL,
ADD COLUMN data_registro DATE NOT NULL;

-- Adding foreign key
ALTER TABLE presencas
ADD CONSTRAINT fk_presencas_inscricao
FOREIGN KEY (inscricao_id) REFERENCES inscricoes(id)
ON DELETE CASCADE ON UPDATE CASCADE;