CREATE TABLE alunos_turmas (
    aluno_id UUID NOT NULL,
    turma_id UUID NOT NULL,
    PRIMARY KEY (aluno_id, turma_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE ON UPDATE CASCADE
);