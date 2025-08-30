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
-- TABELA MODALIDADES
-- ==========================
CREATE TABLE modalidades (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    nome VARCHAR(100) NOT NULL,
    cor_padrao VARCHAR(20),
    usa_graduacao BOOLEAN DEFAULT TRUE
);

-- ==========================
-- TABELA PROFESSORES
-- ==========================
CREATE TABLE professores (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    usuario_id UUID NOT NULL UNIQUE,
    especialidades TEXT,
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
-- RELAÇÃO ALUNO x MODALIDADE (N:N)
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
    dias_semana VARCHAR(50), -- Ex: "SEG,QUA,SEX"
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
    refresh_token BOOLEAN DEFAULT FALSE,
    expirado BOOLEAN DEFAULT FALSE,
    revogado BOOLEAN DEFAULT FALSE,
    criado_em TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expira_em TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE ON UPDATE CASCADE
);