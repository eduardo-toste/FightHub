# FightHub

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**FightHub** é uma plataforma empresarial para gerenciamento de academias de artes marciais, desenvolvida com arquitetura robusta e segurança avançada. A solução oferece uma API REST completa para administração de usuários, modalidades, turmas, aulas e controle de presenças, atendendo às necessidades de academias de todos os portes.

<img width="1422" height="374" alt="Screenshot 2025-09-12 at 20 50 36" src="https://github.com/user-attachments/assets/36ce6cea-f519-4c85-8d03-e56c663d158a" />

## Índice

- [Características](#características)
- [Tecnologias](#tecnologias)
- [Pré-requisitos](#pré-requisitos)
- [Instalação](#instalação)
- [Configuração](#configuração)
- [Uso](#uso)
- [Documentação da API](#documentação-da-api)
- [Arquitetura](#arquitetura)
- [Testes](#testes)
- [Deploy](#deploy)
- [Contribuição](#contribuição)

## Características

Abaixo estão as características concretas implementadas no projeto (baseado no comportamento real das controllers, services e repositórios presentes no código):

### Autenticação e Autorização
- Auth baseado em **JWT (JSON Web Tokens)** com suporte a **refresh tokens** para renovação de sessão.
- Endpoints implementados: `/auth/login`, `/auth/refresh`, `/auth/logout`.
- Fluxo de recuperação de senha com envio de código por e-mail, validação do código e confirmação de nova senha (`/auth/recuperar-senha`, `/auth/recuperar-senha/validar-codigo`, `/auth/recuperar-senha/nova-senha`).
- Ativação de conta por token via endpoint `/ativar` (permitindo definir senha e dados no momento da ativação).
- Criação automática de um usuário administrador na primeira execução (modo dev).
- Controle de acesso por Roles (via Spring Security): `ADMIN`, `COORDENADOR`, `PROFESSOR`, `ALUNO`, `RESPONSAVEL`.

### Gerenciamento de Usuários
- CRUD e listagens paginadas de usuários (`/usuarios`), com endpoints para:
  - consulta e atualização dos próprios dados (`/usuarios/me`, `PUT/PATCH /usuarios/me`),
  - administração de roles (`PATCH /usuarios/{id}/role`) e status (`PATCH /usuarios/{id}/status`) — *Apenas ADMIN*.
- Validações e mensagens de erro padronizadas via DTOs de erro.

### Alunos
- Endpoints para criação, listagem e consulta de alunos (`/alunos`) com paginação.
- Operações específicas: atualizar data de nascimento, data de matrícula, ativar/desativar matrícula (`PATCH /alunos/{id}/data-nascimento`, `/data-matricula`, `/matricula`).
- Promoção e rebaixamento de faixa e grau via endpoints dedicados (`/alunos/{id}/promover/faixa`, `/rebaixar/faixa`, `/promover/grau`, `/rebaixar/grau`).
- Associação de responsáveis a alunos (vínculo e desvínculo).

### Turmas e Professores
- CRUD de turmas com controle de status (ativo/inativo) e soft delete (`/turmas`).
- Vinculação/desvinculação de professores e alunos a turmas (`PATCH`/`DELETE` em `/turmas/{idTurma}/professores/{idProfessor}` e `/turmas/{idTurma}/alunos/{idAluno}`).
- Listagens paginadas e consulta por ID; permissões restritas conforme roles.

### Aulas e Inscrições
- Criação, atualização (com endpoints para atualizar status ou todos os campos), vinculação de turma e exclusão de aulas (`/aulas`).
- Cada aula tem atributo `limite_alunos` e pode ser associada a uma turma.
- Inscrições: inscrição do aluno autenticado em uma aula e cancelamento (`POST/DELETE /aulas/{idAula}/inscricoes`), listagem por aula e listagem das próprias inscrições do aluno (`/aulas/inscricoes/minhas`).
- Endpoints para listar apenas as aulas disponíveis para o aluno ou para o professor autenticado.

### Presenças e métricas de frequência
- Registro de presença por inscrição com flag `presente` (endpoint de atualização por inscrição) e listagens por aula ou do próprio aluno.
- Presenças persistidas com data, usadas para cálculos de métricas no `DashboardService` (presença média geral, presença média por turma, top faltas, etc.).

### Dashboard e Métricas Administrativas
- Endpoint `/admin/dashboard` que agrega múltiplas métricas operacionais:
  - Dados dos alunos: total ativos/inativos, novos nos últimos 30 dias, idade média.
  - Dados das turmas: total de turmas ativas/inativas, ocupação média das aulas (0.0–1.0), percentual de aulas lotadas (>90%), média de alunos por aula.
  - Engajamento no mês: número de aulas previstas, realizadas e canceladas; presença média geral e por turma; top 5 alunos com mais faltas.
- Métricas calculadas no serviço com queries otimizadas nos repositórios e tratamento de `null` (retorna 0.0 quando aplicável) para evitar divisões por zero.

### Observabilidade, documentação e logs
- Swagger/OpenAPI disponível em `/swagger-ui.html` e `/v3/api-docs` (anotações em controllers com exemplos e erros).
- Logs gravados na pasta `logs/`; configuração de nível de log SQL habilitada para desenvolvimento.

### Testes e qualidade de código
- Testes unitários com JUnit5 + Mockito (muitos serviços incluem testes de unidade); testes de integração estão presentes para cenários principais (Aulas, Inscrições, Presenças, Autenticação).
- Relatório de cobertura gerado via JaCoCo (`target/site/jacoco/index.html`).
- Uso de H2 em memória para facilitar testes isolados e TestContainers quando necessário.

### Infra e Deploy
- Execução local via `mvn spring-boot:run` e empacotamento via `mvn clean package`.
- Banco containerizado com `docker-compose.yml` para desenvolvimento; Flyway para migrações.

> Observação: esta seção descreve o comportamento implementado no código atual do projeto (controllers, services e repositórios). Para detalhes das regras de negócio e mensagens de erro específicas, consulte os DTOs e serviços correspondentes (`src/main/java/com/fighthub/service`, `controller`, `dto`).

## Tecnologias

### Backend
- **Java 21** - Linguagem de programação com recursos modernos
- **Spring Boot 3.2.5** - Framework principal para desenvolvimento
- **Spring Security** - Autenticação e autorização robusta com JWT
- **Spring Data JPA** - Persistência de dados com Hibernate
- **PostgreSQL 16** - Banco de dados principal
- **H2 Database** - Banco de dados para testes
- **Flyway** - Migração de banco de dados
- **JWT (jjwt)** - Autenticação stateless com tokens
- **Lombok** - Redução de boilerplate
- **MapStruct** - Mapeamento de objetos DTO ↔ Entity
- **Spring Validation** - Validação de dados e constraints
- **Spring Mail** - Envio de emails para ativação e notificações
- **JaCoCo** - Cobertura de testes
- **HikariCP** - Pool de conexões do banco de dados

### Banco de Dados
- **PostgreSQL 16** - Banco de dados relacional principal
- **Flyway** - Migração e versionamento do banco de dados
- **H2** - Banco em memória para testes automatizados

### Documentação e Testes
- **Swagger/OpenAPI 3** - Documentação interativa da API
- **JaCoCo** - Relatórios de cobertura de testes
- **JUnit 5** - Framework de testes
- **Mockito** - Mocking para testes unitários
- **TestContainers** - Testes de integração com containers


### Ferramentas de Desenvolvimento
- **Maven** - Gerenciamento de dependências e build
- **Docker Compose** - Containerização do banco de dados
- **Git** - Controle de versão

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21** ou superior
- **Maven 3.6+**
- **PostgreSQL 16** ou superior
- **Docker** e **Docker Compose** (opcional, para usar o banco containerizado)

## Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/eduardo-toste/fighthub.git
cd fighthub
```

### 2. Configuração do Banco de Dados

#### Opção A: Usando Docker Compose (Recomendado)

```bash
# Copie o arquivo de exemplo e configure as variáveis
cp .env.example .env

# Edite o arquivo .env com suas configurações
nano .env
```

Exemplo de configuração no `.env`:
```env
POSTGRES_DB=fighthub
POSTGRES_USER=admin
POSTGRES_PASSWORD=111222333
```

```bash
# Inicie o banco de dados
docker-compose up -d
```

#### Opção B: PostgreSQL Local

1. Crie um banco de dados PostgreSQL
2. Configure as credenciais no arquivo `application.properties`

### 3. Configuração da Aplicação

```bash
# Copie o arquivo de configuração
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edite as configurações conforme necessário
nano src/main/resources/application.properties
```

### 4. Executar a Aplicação

```bash
# Compile e execute
mvn spring-boot:run
```

A aplicação estará disponível em: `http://localhost:8080`

## Configuração

### Configurações da Aplicação

Principais configurações no `application.properties`:

```properties
# Banco de Dados
spring.datasource.url=jdbc:postgresql://localhost:5432/fighthub
spring.datasource.username=admin
spring.datasource.password=111222333

# JWT
security.jwt.secret=f4c818a6d3e94c92bd158e3d76c20455
security.jwt.expiration=900000
security.jwt.refresh-expiration=604800000

# Logs
logging.file.path=logs
logging.level.org.hibernate.SQL=DEBUG
```

## Uso

### Endpoints Principais

#### Autenticação (`/auth`)
- `POST /auth/login` - Login do usuário
- `POST /auth/refresh` - Renovar token de acesso
- `POST /auth/logout` - Logout do usuário
- `POST /auth/recuperar-senha` - Solicitar recuperação de senha
- `POST /auth/recuperar-senha/validar-codigo` - Validar código de recuperação
- `POST /auth/recuperar-senha/confirmar` - Confirmar nova senha

#### Usuários (`/usuarios`)
- `GET /usuarios` - Listar usuários (paginado) - *Apenas ADMIN*
- `GET /usuarios/{id}` - Consultar usuário por ID - *Apenas ADMIN*
- `GET /usuarios/me` - Consultar próprios dados
- `PUT /usuarios/{id}` - Atualização completa de usuário - *Apenas ADMIN*
- `PATCH /usuarios/{id}` - Atualização parcial de usuário - *Apenas ADMIN*
- `PUT /usuarios/me` - Atualização completa dos próprios dados
- `PATCH /usuarios/me` - Atualização parcial dos próprios dados
- `PATCH /usuarios/{id}/role` - Atualizar role do usuário - *Apenas ADMIN*
- `PATCH /usuarios/{id}/status` - Atualizar status do usuário - *Apenas ADMIN*
- `PATCH /usuarios/me/password` - Alterar própria senha

#### Alunos (`/alunos`)
- `GET /alunos` - Listar alunos (paginado) - *ADMIN, COORDENADOR, PROFESSOR*
- `GET /alunos/{id}` - Consultar aluno por ID - *ADMIN, COORDENADOR, PROFESSOR*
- `POST /alunos` - Criar novo aluno - *ADMIN, COORDENADOR, PROFESSOR*
- `PATCH /alunos/{id}/matricula` - Atualizar status de matrícula
- `PATCH /alunos/{id}/data-matricula` - Atualizar data de matrícula
- `PATCH /alunos/{id}/data-nascimento` - Atualizar data de nascimento

#### Ativação (`/ativar`)
- `POST /ativar` - Ativar conta do usuário

#### Aulas (`/aulas`)
- `POST /aulas` - Criar nova aula - *ADMIN, PROFESSOR*
- `GET /aulas` - Listar todas as aulas (paginado) - *ADMIN, COORDENADOR, PROFESSOR*
- `GET /aulas/alunos` - Listar aulas disponíveis para o aluno autenticado - *ALUNO*
- `GET /aulas/professores` - Listar aulas ministradas pelo professor autenticado - *PROFESSOR*
- `GET /aulas/{id}` - Obter detalhes da aula por ID - *ADMIN, COORDENADOR, PROFESSOR*
- `PATCH /aulas/{id}/status` - Atualizar status (ativar/inativar) - *ADMIN, PROFESSOR*
- `PUT /aulas/{id}` - Atualização completa da aula - *ADMIN, PROFESSOR*
- `PATCH /aulas/{idAula}/turmas/{idTurma}` - Vincular aula a turma - *ADMIN, PROFESSOR*
- `DELETE /aulas/{idAula}/turmas/{idTurma}` - Desvincular aula de turma - *ADMIN, PROFESSOR*
- `DELETE /aulas/{id}` - Excluir aula - *ADMIN, PROFESSOR*

#### Inscrições
- `POST /aulas/{idAula}/inscricoes` - Inscrever aluno autenticado em uma aula - *ALUNO*
- `DELETE /aulas/{idAula}/inscricoes` - Cancelar inscrição do aluno autenticado - *ALUNO*
- `GET /aulas/{idAula}/inscricoes` - Listar inscrições de uma aula (paginado) - *ADMIN, COORDENADOR, PROFESSOR*
- `GET /aulas/inscricoes/minhas` - Minhas inscrições (paginado) - *ALUNO*

#### Presenças
- `PATCH /aulas/{idAula}/presencas/inscricao/{idInscricao}` - Atualizar presença (presente/ausente) por inscrição - *ADMIN, PROFESSOR*
- `GET /aulas/{idAula}/presencas` - Listar presenças de uma aula (paginado) - *ADMIN, PROFESSOR*
- `GET /aulas/me/presencas` - Listar minhas presenças (paginado) - *ALUNO*

#### Turmas (`/turmas`)
- `POST /turmas` - Criar nova turma - *ADMIN, COORDENADOR*
- `GET /turmas` - Listar turmas (paginado) - *ADMIN, COORDENADOR, PROFESSOR*
- `GET /turmas/{id}` - Buscar turma por ID - *ADMIN, COORDENADOR, PROFESSOR*
- `PUT /turmas/{id}` - Atualização completa de turma - *ADMIN, COORDENADOR*
- `PATCH /turmas/{id}/status` - Atualizar status da turma - *ADMIN, COORDENADOR*
- `DELETE /turmas/{id}` - Excluir turma (soft delete) - *ADMIN, COORDENADOR*
- `PATCH /turmas/{idTurma}/professores/{idProfessor}` - Vincular professor à turma - *ADMIN, COORDENADOR*
- `DELETE /turmas/{idTurma}/professores/{idProfessor}` - Desvincular professor - *ADMIN, COORDENADOR*
- `PATCH /turmas/{idTurma}/alunos/{idAluno}` - Vincular aluno à turma - *ADMIN, COORDENADOR*
- `DELETE /turmas/{idTurma}/alunos/{idAluno}` - Desvincular aluno - *ADMIN, COORDENADOR*

#### Professores (`/professores`)
- `POST /professores` - Criar professor - *ADMIN, COORDENADOR*
- `GET /professores` - Listar professores (paginado) - *ADMIN, COORDENADOR*
- `GET /professores/{id}` - Buscar professor por ID - *ADMIN, COORDENADOR*

#### Responsáveis (`/responsaveis`)
- `POST /responsaveis` - Criar responsável - *ADMIN, COORDENADOR*
- `GET /responsaveis` - Listar responsáveis (paginado) - *ADMIN, COORDENADOR*
- `GET /responsaveis/{id}` - Buscar responsável por ID - *ADMIN, COORDENADOR*
- `PATCH /responsaveis/{idResponsavel}/alunos/{idAluno}` - Vincular aluno ao responsável - *ADMIN, COORDENADOR*
- `DELETE /responsaveis/{idResponsavel}/alunos/{idAluno}` - Remover vínculo - *ADMIN, COORDENADOR*

#### Dashboard (`/admin/dashboard`)
- `GET /admin/dashboard` - Retorna dados agregados para a visão administrativa do sistema. Campos principais retornados no JSON:
  - `dadosAlunos`: totais e média de idade
  - `dadosTurmas`: total de turmas, ocupação média, % de aulas lotadas (>90%), média de alunos por aula
  - `dadosEngajamento`: aulas previstas/realizadas/canceladas no mês, presença média geral e por turma, top5 alunos com mais faltas
  - Requer role: `ADMIN` ou `COORDENADOR`

### DTOs do Dashboard (resumo)
- `DashboardResponse`
  - `dadosAlunos`: `AlunosDashboardResponse`
  - `dadosTurmas`: `TurmasDashboardResponse`
  - `dadosEngajamento`: `EngajamentoDashboardResponse`

- `AlunosDashboardResponse` (exemplo)
  - `totalAlunosAtivos` (long)
  - `totalAlunosInativos` (long)
  - `novosAlunosUltimos30Dias` (long)
  - `idadeMediaAlunos` (int)

- `TurmasDashboardResponse` (exemplo)
  - `totalTurmasAtivas` (long)
  - `totalTurmasInativas` (long)
  - `ocupacaoMediaTurmas` (double) — média de ocupação (0.0 - 1.0)
  - `percentualAulasLotadas` (double) — porcentagem de aulas com ocupação > 90 (0.0 - 100.0)
  - `mediaAlunosPorAula` (double)

- `EngajamentoDashboardResponse` (exemplo)
  - `aulasPrevistasNoMes` (long)
  - `aulasRealizadasNoMes` (long)
  - `aulasCanceladasNoMes` (long)
  - `presencaMediaGeralNoMes` (double) — presença média considerando todas as turmas (0.0 - 100.0)
  - `presencaMediaPorTurmaNoMes` (double) — média de presença por turma (0.0 - 100.0)
  - `top5AlunosComMaisFaltasNoMes` (List<AlunoFaltas>) — lista com os 5 alunos e número de faltas

> Observação: valores nulos retornados pelos repositórios são tratados no serviço e substituídos por 0.0 quando aplicável.

#### Exemplo de Login

```bash
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "email": "admin@fighthub.com",
    "senha": "123456"
  }'
```

### Usuário Padrão

Após a primeira execução, um usuário administrador é criado automaticamente:

- **Email**: `admin@fighthub.com`
- **Senha**: `123456`
- **Role**: `ADMIN`

## Documentação da API

A documentação completa da API está disponível através do Swagger UI:

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`

### Autenticação na API

Para acessar endpoints protegidos, inclua o token JWT no header:

```bash
curl -X GET http://localhost:8080/api/usuarios \
  -H "Authorization: Bearer SEU_JWT_TOKEN"
```

## Arquitetura

```
src/
├── main/
│   ├── java/com/fighthub/
│   │   ├── config/          # Configurações (Security, OpenAPI)
│   │   ├── controller/      # Controllers REST
│   │   ├── dto/            # Data Transfer Objects
│   │   ├── exception/      # Tratamento de exceções
│   │   ├── mapper/         # Mappers para conversão de objetos
│   │   ├── model/          # Entidades JPA
│   │   ├── repository/     # Repositórios de dados
│   │   ├── security/       # Configurações de segurança
│   │   ├── service/        # Lógica de negócio
│   │   └── utils/          # Utilitários
│   └── resources/
│       ├── db/migration/   # Scripts de migração Flyway
│       ├── static/         # Arquivos estáticos
│       └── templates/      # Templates (se houver)
└── test/                   # Testes unitários e de integração
```

## Testes

### Executar Todos os Testes

```bash
mvn test
```

### Executar Testes com Cobertura

```bash
mvn clean test jacoco:report
```

O relatório de cobertura será gerado em: `target/site/jacoco/index.html`

### Tipos de Testes

- **Testes Unitários**: Testam componentes isolados (18 classes de teste)
- **Testes de Integração**: Testam fluxos completos
- **Testes de Repositório**: Testam acesso aos dados
- **Testes de Segurança**: Testam autenticação e autorização

## Deploy

### Ambiente de Produção

Para deploy em ambiente de produção, siga estas diretrizes:

1. **Configuração do Banco de Dados**
   - Configure um PostgreSQL dedicado
   - Execute as migrações do Flyway
   - Configure backup automático

2. **Variáveis de Ambiente**
   - Configure todas as variáveis de ambiente necessárias
   - Use secrets management para dados sensíveis
   - Configure logs centralizados

3. **Segurança**
   - Configure HTTPS/TLS
   - Use certificados SSL válidos
   - Configure firewall e rate limiting
   - Altere as chaves JWT padrão

4. **Monitoramento**
   - Configure health checks
   - Implemente métricas de aplicação
   - Configure alertas de sistema

### Docker

```bash
# Build da aplicação
mvn clean package

# Build da imagem Docker
docker build -t fighthub:latest .

# Executar com Docker Compose
docker-compose -f docker-compose.prod.yml up -d
```

## Contribuição

Contribuições são sempre bem-vindas! Para contribuir:

1. **Fork** o projeto
2. Crie uma **branch** para sua feature (`git checkout -b feature/AmazingFeature`)
3. **Commit** suas mudanças (`git commit -m 'Add some AmazingFeature'`)
4. **Push** para a branch (`git push origin feature/AmazingFeature`)
5. Abra um **Pull Request**

### Padrões de Código

- Siga as convenções do Java
- Use Lombok para reduzir boilerplate
- Escreva testes para novas funcionalidades
- Mantenha a cobertura de testes acima de 80%
- Documente APIs com Swagger/OpenAPI
- Use commits semânticos

**FightHub** - Solução empresarial para gerenciamento de academias de artes marciais.