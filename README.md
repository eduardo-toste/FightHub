<div align="center">

# ⚔️ FightHub — Backend API

**Plataforma empresarial para gerenciamento de academias de Jiu-Jitsu**

[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring_Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring_Security-JWT-6DB33F?style=for-the-badge&logo=spring-security&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Maven](https://img.shields.io/badge/Maven-3.6+-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

[📖 Documentação da API](#-documentação-da-api) •
[🚀 Instalação](#-instalação) •
[⚙️ Configuração](#️-configuração) •
[🏗️ Arquitetura](#️-arquitetura) •
[🧪 Testes](#-testes) •
[🖥️ Frontend](#️-frontend)

</div>

---

## 📋 Índice

- [Sobre o Projeto](#-sobre-o-projeto)
- [Frontend](#-frontend)
- [Funcionalidades](#-funcionalidades)
- [Stack Tecnológica](#-stack-tecnológica)
- [Arquitetura](#-arquitetura)
- [Modelo de Domínio](#-modelo-de-domínio)
- [Pré-requisitos](#-pré-requisitos)
- [Instalação](#-instalação)
- [Configuração](#-configuração)
- [Documentação da API](#-documentação-da-api)
- [Segurança](#-segurança)
- [Testes](#-testes)
- [Deploy](#-deploy)
- [Contribuição](#-contribuição)

---

## 💡 Sobre o Projeto

O **FightHub** é uma API REST robusta e escalável projetada para o gerenciamento completo de academias de Jiu-Jitsu. A plataforma cobre desde o controle de alunos e turmas até métricas avançadas de desempenho e engajamento, com segurança de nível empresarial baseada em JWT e controle de acesso granular por perfis.

### Por que FightHub?

- 🔐 **Segurança avançada** — Autenticação stateless com JWT e Refresh Tokens, BCrypt e proteção contra acessos não autorizados
- 📊 **Dashboard analítico** — Métricas operacionais em tempo real sobre alunos, turmas e presença
- 📧 **Comunicação integrada** — Envio automático de e-mails transacionais via Thymeleaf (ativação, boas-vindas, recuperação de senha)
- 🥋 **Domínio rico** — Gestão completa de graduações (faixas e graus do Jiu-Jitsu), presenças, inscrições e turmas
- 🔍 **CEP automático** — Integração com a API ViaCEP para preenchimento automático de endereços
- 📁 **Upload de arquivos** — Gerenciamento de fotos de perfil com armazenamento local
- 🗄️ **Migrações versionadas** — Controle de schema com Flyway (15+ migrações)

---

## 🖥️ Frontend

O frontend desta plataforma é desenvolvido em um repositório separado:

> 🔗 **[FightHub — Frontend](https://github.com/eduardo-toste/FightHubUI)**

---

## ✨ Funcionalidades

### 🔑 Autenticação e Autorização
- Login com JWT e Refresh Token (renovação automática de sessão)
- Logout com revogação de tokens
- Ativação de conta por link enviado por e-mail
- Fluxo completo de recuperação de senha (solicitação → validação de código → nova senha)
- Controle de acesso por roles via `@PreAuthorize` (Spring Security)

### 👥 Gestão de Usuários
- CRUD completo com atualização total e parcial (`PUT` / `PATCH`)
- Gerenciamento dos próprios dados (`/usuarios/me`)
- Upload e remoção de foto de perfil
- Alteração de role e status por administradores
- Busca de endereço por CEP (integração ViaCEP)

### 🎓 Alunos
- Cadastro com validação de CPF único e e-mail único
- Controle automático de menoridade com vinculação obrigatória de responsáveis
- Graduação por faixas: **Branca → Cinza → Amarela → Laranja → Verde → Azul → Roxa → Marrom → Preta**
- Graduação por graus: **0 → I → II → III → IV**
- Promoção e rebaixamento de faixa/grau
- Gestão de matrícula (ativação, desativação, data de matrícula e nascimento)
- Listagem paginada com todos os dados do aluno

### 🧑‍🏫 Professores e Responsáveis
- Cadastro e listagem paginada de professores
- Cadastro de responsáveis com vínculo a múltiplos alunos menores de idade

### 🏫 Turmas
- CRUD completo com soft delete
- Vinculação de professor à turma
- Inscrição e remoção de alunos
- Controle de status (ativo/inativo)

### 📅 Aulas e Inscrições
- Criação de aulas com limite de vagas, data/hora e status
- Status de aula: `DISPONIVEL`, `AGENDADA`, `PENDENTE`, `EM_PROGRESSO`, `CANCELADA`, `FINALIZADA`
- Vinculação de aulas a turmas
- Inscrição e cancelamento de inscrição pelo próprio aluno
- Controle de status da inscrição: `INSCRITO`, `CANCELADO`, `DESMARCADO`
- Listagem de aulas disponíveis filtradas por aluno ou professor autenticado

### ✅ Presenças
- Registro de presença por inscrição (`presente` / `ausente`)
- Listagem de presenças por aula ou pelo próprio aluno
- Dados de presença alimentam o Dashboard analítico

### 📊 Dashboard Administrativo
Endpoint `/admin/dashboard` retorna métricas agregadas em tempo real:

| Categoria | Métricas |
|-----------|----------|
| **Alunos** | Total ativos/inativos, novos nos últimos 30 dias, idade média |
| **Turmas** | Total ativas/inativas, ocupação média, % de aulas lotadas (>90%), média alunos/aula |
| **Engajamento** | Aulas previstas/realizadas/canceladas no mês, presença média geral e por turma, Top 5 faltas |

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia | Versão |
|--------|-----------|--------|
| Linguagem | Java | 21 |
| Framework | Spring Boot | 3.2.5 |
| Segurança | Spring Security + JJWT | 0.11.5 |
| Persistência | Spring Data JPA + Hibernate | — |
| Banco de Dados | PostgreSQL | 16 |
| Banco (testes) | H2 In-Memory | — |
| Migrações | Flyway | 10.14.0 |
| Templates de E-mail | Thymeleaf | — |
| Documentação | SpringDoc OpenAPI (Swagger UI) | 2.6.0 |
| Boilerplate | Lombok | 1.18.36 |
| Build | Maven | 3.6+ |
| Testes Unitários | JUnit 5 + Mockito | 5.12.0 |
| Cobertura | JaCoCo | 0.8.13 |
| Containerização | Docker + Docker Compose | — |
| CEP | Integração ViaCEP | — |
| Reativos | Spring WebFlux (WebClient) | — |

---

## 🏗️ Arquitetura

O projeto segue uma arquitetura em camadas bem definida, com separação clara de responsabilidades:

```
src/
├── main/
│   ├── java/com/fighthub/
│   │   ├── config/           # Configurações (Security, OpenAPI, CORS)
│   │   ├── controller/       # Controllers REST (entrada HTTP)
│   │   │   ├── AlunoController.java
│   │   │   ├── AulaController.java
│   │   │   ├── AuthController.java
│   │   │   ├── DashboardController.java
│   │   │   ├── EnderecoController.java
│   │   │   ├── InscricaoController.java
│   │   │   ├── PresencaController.java
│   │   │   ├── ProfessorController.java
│   │   │   ├── ResponsavelController.java
│   │   │   ├── TurmaController.java
│   │   │   └── UsuarioController.java
│   │   ├── docs/             # Configurações do Swagger/OpenAPI
│   │   ├── dto/              # Data Transfer Objects (Request/Response)
│   │   ├── exception/        # Exceções customizadas e handlers globais
│   │   ├── integration/      # Integrações externas (ViaCEP)
│   │   ├── mapper/           # Conversão Entity ↔ DTO
│   │   ├── model/            # Entidades JPA
│   │   │   └── enums/        # Enumerações de domínio
│   │   ├── repository/       # Repositórios Spring Data JPA
│   │   ├── security/         # Filtros e handlers de segurança
│   │   ├── service/          # Lógica de negócio
│   │   └── utils/            # Utilitários
│   └── resources/
│       ├── db/migration/     # Scripts Flyway (V1 → V15)
│       └── templates/        # Templates Thymeleaf para e-mails
└── test/
    ├── java/com/fighthub/
    │   ├── service/          # 15 classes de testes unitários
    │   ├── integration/      # 10 classes de testes de integração
    │   └── config/           # Configurações de teste
    └── resources/
        └── application-test.properties
```

### Fluxo de uma Requisição

```
HTTP Request
    │
    ▼
SecurityFilter (JWT Validation)
    │
    ▼
Controller (Validação de input, @PreAuthorize)
    │
    ▼
Service (Regras de negócio, @Transactional)
    │
    ▼
Repository (Spring Data JPA → PostgreSQL)
    │
    ▼
Response DTO (Mapeamento via Mapper)
    │
    ▼
HTTP Response
```

---

## 🗄️ Modelo de Domínio

```
Usuario (1) ──────── (1) Aluno
                          │
                    ┌─────┴──────┐
                    │            │
             GraduacaoAluno  List<Turma>
              (faixa + grau)
                    │
               List<Responsavel>
                    │
              List<Inscricao>
                    │
                 Presenca

Turma ──── Professor
  │
  └──── List<Aula>
              │
         List<Inscricao> ──── Presenca
```

**Enumerações de domínio:**
- `Role`: `ADMIN` | `COORDENADOR` | `PROFESSOR` | `ALUNO` | `RESPONSAVEL`
- `BeltGraduation`: `BRANCA` → `CINZA` → `AMARELA` → `LARANJA` → `VERDE` → `AZUL` → `ROXA` → `MARROM` → `PRETA`
- `GraduationLevel`: `ZERO` | `I` | `II` | `III` | `IV`
- `ClassStatus`: `DISPONIVEL` | `AGENDADA` | `PENDENTE` | `EM_PROGRESSO` | `CANCELADA` | `FINALIZADA`
- `SubscriptionStatus`: `INSCRITO` | `CANCELADO` | `DESMARCADO`
- `TokenType`: `ACCESS` | `REFRESH`

---

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

| Ferramenta | Versão mínima | Download |
|-----------|--------------|---------|
| Java (JDK) | 21 | [Adoptium](https://adoptium.net/) |
| Maven | 3.6+ | [maven.apache.org](https://maven.apache.org/) |
| Docker + Compose | Qualquer | [docker.com](https://www.docker.com/) |
| PostgreSQL | 16 (ou via Docker) | [postgresql.org](https://www.postgresql.org/) |

---

## 🚀 Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/eduardo-toste/FightHub.git
cd FightHub
```

### 2. Configure o banco de dados

#### Opção A — Docker Compose (Recomendado)

```bash
# Suba o PostgreSQL em container
docker-compose up -d
```

#### Opção B — PostgreSQL local

Crie um banco de dados chamado `fighthub` e configure as credenciais no `application.properties`.

### 3. Configure a aplicação

```bash
# Copie o arquivo de exemplo
cp src/main/resources/application.properties.example src/main/resources/application.properties

# Edite com suas configurações reais
nano src/main/resources/application.properties
```

### 4. Execute a aplicação

```bash
mvn spring-boot:run
```

A API estará disponível em: **`http://localhost:8080`**

A documentação Swagger em: **`http://localhost:8080/swagger-ui.html`**

---

## ⚙️ Configuração

### Variáveis do `application.properties`

```properties
# ==================== DATABASE ====================
spring.datasource.url=jdbc:postgresql://localhost:5432/fighthub
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha

# ==================== JWT ====================
# Chave secreta para assinar tokens (use uma chave forte em produção)
security.jwt.secret=sua_chave_secreta_256bits
# Access token: 15 minutos (ms)
security.jwt.expiration=900000
# Refresh token: 7 dias (ms)
security.jwt.refresh-expiration=604800000

# ==================== MAIL ====================
mail.host=smtp.gmail.com
mail.port=587
mail.username=seu_email@gmail.com
mail.password=sua_senha_de_app
mail.tls-enabled=true

# ==================== UPLOADS ====================
uploads.profile-dir=uploads/profiles

# ==================== LOGS ====================
logging.file.path=logs
logging.level.org.hibernate.SQL=DEBUG
```

### Usuário padrão (criado automaticamente)

Após a primeira execução, um administrador é criado:

```
E-mail:  admin@fighthub.com
Senha:   123456
Role:    ADMIN
```

> ⚠️ **Altere as credenciais padrão imediatamente em ambientes de produção.**

---

## 📖 Documentação da API

A documentação interativa completa está disponível via **Swagger UI**:

> 🔗 **`http://localhost:8080/swagger-ui.html`**

### Endpoints Resumidos

<details>
<summary><b>🔑 Autenticação</b> — <code>/auth</code></summary>

| Método | Endpoint | Descrição | Auth |
|--------|----------|-----------|------|
| `POST` | `/auth/login` | Login do usuário | ❌ |
| `POST` | `/auth/refresh` | Renovar access token | ❌ |
| `POST` | `/auth/logout` | Logout e revogação do token | ✅ |
| `POST` | `/auth/recuperar-senha` | Solicitar código de recuperação | ❌ |
| `POST` | `/auth/recuperar-senha/validar-codigo` | Validar código recebido por e-mail | ❌ |
| `POST` | `/auth/recuperar-senha/confirmar` | Confirmar nova senha | ❌ |
| `POST` | `/ativar` | Ativar conta via token de e-mail | ❌ |

</details>

<details>
<summary><b>👤 Usuários</b> — <code>/usuarios</code></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `GET` | `/usuarios` | Listar usuários (paginado) | ADMIN |
| `GET` | `/usuarios/{id}` | Buscar usuário por ID | ADMIN |
| `GET` | `/usuarios/me` | Dados do usuário autenticado | Todos |
| `PUT` | `/usuarios/{id}` | Atualizar usuário (completo) | ADMIN |
| `PATCH` | `/usuarios/{id}` | Atualizar usuário (parcial) | ADMIN |
| `PUT` | `/usuarios/me` | Atualizar próprios dados (completo) | Todos |
| `PATCH` | `/usuarios/me` | Atualizar próprios dados (parcial) | Todos |
| `PATCH` | `/usuarios/{id}/role` | Alterar role | ADMIN |
| `PATCH` | `/usuarios/{id}/status` | Alterar status | ADMIN |
| `PATCH` | `/usuarios/me/password` | Alterar própria senha | Todos |
| `POST` | `/usuarios/me/foto` | Upload de foto de perfil | Todos |
| `DELETE` | `/usuarios/me/foto` | Remover foto de perfil | Todos |

</details>

<details>
<summary><b>🎓 Alunos</b> — <code>/alunos</code></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `POST` | `/alunos` | Criar aluno | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/alunos` | Listar alunos (paginado) | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/alunos/{id}` | Buscar aluno por ID | ADMIN, COORDENADOR, PROFESSOR |
| `PATCH` | `/alunos/{id}/matricula` | Ativar/desativar matrícula | ADMIN, COORDENADOR |
| `PATCH` | `/alunos/{id}/data-matricula` | Atualizar data de matrícula | ADMIN, COORDENADOR |
| `PATCH` | `/alunos/{id}/data-nascimento` | Atualizar data de nascimento | ADMIN, COORDENADOR |
| `PATCH` | `/alunos/{id}/promover/faixa` | Promover faixa | ADMIN, PROFESSOR |
| `PATCH` | `/alunos/{id}/rebaixar/faixa` | Rebaixar faixa | ADMIN, PROFESSOR |
| `PATCH` | `/alunos/{id}/promover/grau` | Promover grau | ADMIN, PROFESSOR |
| `PATCH` | `/alunos/{id}/rebaixar/grau` | Rebaixar grau | ADMIN, PROFESSOR |

</details>

<details>
<summary><b>🏫 Turmas</b> — <code>/turmas</code></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `POST` | `/turmas` | Criar turma | ADMIN, COORDENADOR |
| `GET` | `/turmas` | Listar turmas (paginado) | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/turmas/{id}` | Buscar turma por ID | ADMIN, COORDENADOR, PROFESSOR |
| `PUT` | `/turmas/{id}` | Atualizar turma (completo) | ADMIN, COORDENADOR |
| `PATCH` | `/turmas/{id}/status` | Atualizar status | ADMIN, COORDENADOR |
| `DELETE` | `/turmas/{id}` | Excluir turma (soft delete) | ADMIN, COORDENADOR |
| `PATCH` | `/turmas/{idTurma}/professores/{idProfessor}` | Vincular professor | ADMIN, COORDENADOR |
| `DELETE` | `/turmas/{idTurma}/professores/{idProfessor}` | Desvincular professor | ADMIN, COORDENADOR |
| `PATCH` | `/turmas/{idTurma}/alunos/{idAluno}` | Vincular aluno | ADMIN, COORDENADOR |
| `DELETE` | `/turmas/{idTurma}/alunos/{idAluno}` | Desvincular aluno | ADMIN, COORDENADOR |

</details>

<details>
<summary><b>📅 Aulas, Inscrições e Presenças</b></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `POST` | `/aulas` | Criar aula | ADMIN, PROFESSOR |
| `GET` | `/aulas` | Listar aulas (paginado) | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/aulas/{id}` | Buscar aula por ID | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/aulas/alunos` | Aulas disponíveis para aluno | ALUNO |
| `GET` | `/aulas/professores` | Aulas do professor autenticado | PROFESSOR |
| `PUT` | `/aulas/{id}` | Atualizar aula (completo) | ADMIN, PROFESSOR |
| `PATCH` | `/aulas/{id}/status` | Atualizar status da aula | ADMIN, PROFESSOR |
| `PATCH` | `/aulas/{idAula}/turmas/{idTurma}` | Vincular aula a turma | ADMIN, PROFESSOR |
| `DELETE` | `/aulas/{idAula}/turmas/{idTurma}` | Desvincular aula de turma | ADMIN, PROFESSOR |
| `DELETE` | `/aulas/{id}` | Excluir aula | ADMIN, PROFESSOR |
| `POST` | `/aulas/{idAula}/inscricoes` | Inscrever aluno em aula | ALUNO |
| `DELETE` | `/aulas/{idAula}/inscricoes` | Cancelar inscrição | ALUNO |
| `GET` | `/aulas/{idAula}/inscricoes` | Listar inscrições da aula | ADMIN, COORDENADOR, PROFESSOR |
| `GET` | `/aulas/inscricoes/minhas` | Minhas inscrições | ALUNO |
| `PATCH` | `/aulas/{idAula}/presencas/inscricao/{idInscricao}` | Registrar presença | ADMIN, PROFESSOR |
| `GET` | `/aulas/{idAula}/presencas` | Listar presenças da aula | ADMIN, PROFESSOR |
| `GET` | `/aulas/me/presencas` | Minhas presenças | ALUNO |

</details>

<details>
<summary><b>🏫 Professores e Responsáveis</b></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `POST` | `/professores` | Criar professor | ADMIN, COORDENADOR |
| `GET` | `/professores` | Listar professores | ADMIN, COORDENADOR |
| `GET` | `/professores/{id}` | Buscar professor por ID | ADMIN, COORDENADOR |
| `POST` | `/responsaveis` | Criar responsável | ADMIN, COORDENADOR |
| `GET` | `/responsaveis` | Listar responsáveis | ADMIN, COORDENADOR |
| `GET` | `/responsaveis/{id}` | Buscar responsável por ID | ADMIN, COORDENADOR |
| `PATCH` | `/responsaveis/{idResponsavel}/alunos/{idAluno}` | Vincular aluno | ADMIN, COORDENADOR |
| `DELETE` | `/responsaveis/{idResponsavel}/alunos/{idAluno}` | Remover vínculo | ADMIN, COORDENADOR |

</details>

<details>
<summary><b>📊 Dashboard e Endereço</b></summary>

| Método | Endpoint | Descrição | Roles |
|--------|----------|-----------|-------|
| `GET` | `/admin/dashboard` | Métricas operacionais agregadas | ADMIN, COORDENADOR |
| `GET` | `/enderecos/cep/{cep}` | Buscar endereço por CEP (ViaCEP) | ❌ |

</details>

### Exemplo de Uso

```bash
# 1. Login
curl -X POST http://localhost:8080/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email": "admin@fighthub.com", "senha": "123456"}'

# 2. Use o token retornado nas próximas requisições
curl -X GET http://localhost:8080/alunos \
  -H "Authorization: Bearer SEU_ACCESS_TOKEN"
```

---

## 🔐 Segurança

### Fluxo de Autenticação

```
1. POST /auth/login  →  { accessToken, refreshToken }
2. Requisições:       →  Header: Authorization: Bearer <accessToken>
3. Token expirado:    →  POST /auth/refresh  →  { accessToken }
4. POST /auth/logout  →  Tokens revogados no banco
```

### Mecanismos implementados

- **BCrypt** para hash de senhas
- **JWT stateless** com segredo configurável
- **Access Token**: expiração em 15 minutos
- **Refresh Token**: expiração em 7 dias, persistido no banco
- **Revogação explícita** de tokens no logout
- **`SecurityFilter`** customizado executado antes de cada requisição
- **`CustomAuthenticationEntryPoint`** — resposta padronizada para `401 Unauthorized`
- **`CustomAccessDeniedHandler`** — resposta padronizada para `403 Forbidden`
- **CORS** configurado via `CorsConfigurationSource`
- Sessão **STATELESS** (sem HttpSession)

---

## 🧪 Testes

### Executar testes

```bash
# Todos os testes
mvn test

# Testes com relatório de cobertura (JaCoCo)
mvn clean verify

# Relatório disponível em:
# target/site/jacoco/index.html
```

### Cobertura de testes

| Tipo | Classes | Descrição |
|------|---------|-----------|
| **Unitários** | 15 | Um arquivo por service (`AlunoServiceTest`, `AuthServiceTest`, `DashboardServiceTest`, etc.) |
| **Integração** | 10 | Testes end-to-end com MockMvc + H2 (`AlunoIntegrationTest`, `AulaIntegrationTest`, etc.) |

### Tecnologias de teste

- **JUnit 5** — Framework de testes
- **Mockito 5.12** — Mocking de dependências
- **Spring Security Test** — Testes de endpoints protegidos
- **H2 In-Memory** — Banco de dados isolado para testes de integração
- **JaCoCo 0.8.13** — Relatório de cobertura de código

---

## 🐳 Deploy

### Desenvolvimento local com Docker

```bash
# Suba somente o banco de dados
docker-compose up -d

# Execute a aplicação via Maven
mvn spring-boot:run
```

### Produção

```bash
# 1. Gere o artefato
mvn clean package -DskipTests

# 2. Execute o JAR
java -jar target/fighthub-0.0.1-SNAPSHOT.jar \
  --spring.profiles.active=prod
```

### Checklist para produção

- [ ] Altere o `security.jwt.secret` para uma chave forte de 256 bits
- [ ] Configure as variáveis de e-mail SMTP
- [ ] Remova ou restrinja o acesso ao Swagger UI
- [ ] Configure HTTPS/TLS com certificado válido
- [ ] Ajuste o nível de log para `INFO` (não `DEBUG`)
- [ ] Configure backup automático do PostgreSQL
- [ ] Altere as credenciais do usuário `admin@fighthub.com`

---

## 🤝 Contribuição

Contribuições são bem-vindas! Siga os passos abaixo:

1. **Fork** o repositório
2. Crie uma branch para sua feature
   ```bash
   git checkout -b feature/minha-feature
   ```
3. Faça o commit das suas alterações
   ```bash
   git commit -m "feat: adiciona minha nova feature"
   ```
4. Envie para o repositório remoto
   ```bash
   git push origin feature/minha-feature
   ```
5. Abra um **Pull Request**

### Padrões do projeto

- Commits semânticos (`feat:`, `fix:`, `docs:`, `refactor:`, `test:`)
- Cobertura de testes para novas funcionalidades
- Documentação Swagger para novos endpoints
- Código em português (domínio) com inglês técnico (infraestrutura)

---