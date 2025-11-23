CREATE TABLE inscricoes (
    id UUID PRIMARY KEY,
    aluno_id UUID NOT NULL,
    aula_id UUID NOT NULL,
    status VARCHAR(30),
    data_inscricao DATE NOT NULL,
    UNIQUE (aluno_id, aula_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (aula_id) REFERENCES aulas(id) ON DELETE CASCADE ON UPDATE CASCADE
);