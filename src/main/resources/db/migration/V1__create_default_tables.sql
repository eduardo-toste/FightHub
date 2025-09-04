-- ==========================
-- HABILITA EXTENSÃO UUID
-- ==========================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ==========================
-- TABELA USUARIOS
-- ==========================
CREATE TABLE usuarios (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255),
    foto VARCHAR(255),
    role VARCHAR(30) NOT NULL CHECK (role IN ('ADMIN', 'PROFESSOR', 'ALUNO', 'RESPONSAVEL', 'COORDENADOR')),
    login_social BOOLEAN DEFAULT FALSE,
    ativo BOOLEAN DEFAULT TRUE
);

-- ==========================
-- TABELA RESPONSAVEIS
-- ==========================
CREATE TABLE responsaveis (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA ALUNOS
-- ==========================
CREATE TABLE alunos (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL UNIQUE,
    data_matricula DATE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- RELAÇÃO N:N ALUNO x RESPONSAVEL
-- ==========================
CREATE TABLE alunos_responsaveis (
    aluno_id UUID NOT NULL,
    responsavel_id UUID NOT NULL,
    PRIMARY KEY (aluno_id, responsavel_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (responsavel_id) REFERENCES responsaveis(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA MODALIDADES
-- ==========================
CREATE TABLE modalidades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL UNIQUE,
    cor_padrao VARCHAR(20),
    usa_graduacao BOOLEAN DEFAULT TRUE
);

-- ==========================
-- TABELA ESPECIALIDADES
-- ==========================
CREATE TABLE especialidades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    modalidade_id UUID NOT NULL,
    UNIQUE (nome, modalidade_id),
    FOREIGN KEY (modalidade_id) REFERENCES modalidades(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA PROFESSORES
-- ==========================
CREATE TABLE professores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL UNIQUE,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA PROFESSORES x ESPECIALIDADES (N:N)
-- ==========================
CREATE TABLE professores_especialidades (
    professor_id UUID NOT NULL,
    especialidade_id UUID NOT NULL,
    PRIMARY KEY (professor_id, especialidade_id),
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (especialidade_id) REFERENCES especialidades(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA ALUNOS x MODALIDADES (N:N)
-- ==========================
CREATE TABLE alunos_modalidades (
    aluno_id UUID NOT NULL,
    modalidade_id UUID NOT NULL,
    faixa_atual VARCHAR(50),
    PRIMARY KEY (aluno_id, modalidade_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (modalidade_id) REFERENCES modalidades(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA TURMAS
-- ==========================
CREATE TABLE turmas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    modalidade_id UUID NOT NULL,
    professor_id UUID NOT NULL,
    dias_semana VARCHAR(50),
    horario TIME,
    FOREIGN KEY (modalidade_id) REFERENCES modalidades(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA AULAS
-- ==========================
CREATE TABLE aulas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    turma_id UUID NOT NULL,
    professor_id UUID NOT NULL,
    data_hora TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ABERTA', 'CANCELADA', 'REALIZADA')),
    FOREIGN KEY (turma_id) REFERENCES turmas(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (professor_id) REFERENCES professores(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA INSCRICAO_AULA
-- ==========================
CREATE TABLE inscricao_aula (
    aluno_id UUID NOT NULL,
    aula_id UUID NOT NULL,
    data_inscricao TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (aluno_id, aula_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (aula_id) REFERENCES aulas(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA PRESENCAS
-- ==========================
CREATE TABLE presencas (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    aluno_id UUID NOT NULL,
    aula_id UUID NOT NULL,
    presente BOOLEAN DEFAULT FALSE,
    UNIQUE (aluno_id, aula_id),
    FOREIGN KEY (aluno_id) REFERENCES alunos(id) ON DELETE CASCADE ON UPDATE CASCADE,
    FOREIGN KEY (aula_id) REFERENCES aulas(id) ON DELETE CASCADE ON UPDATE CASCADE
);

-- ==========================
-- TABELA TOKENS
-- ==========================
CREATE TABLE tokens (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL,
    token TEXT NOT NULL UNIQUE,
    token_type TEXT NOT NULL,
    expirado BOOLEAN DEFAULT FALSE,
    revogado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);