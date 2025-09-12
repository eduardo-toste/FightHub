# FightHub

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.5-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

**FightHub** é uma plataforma empresarial para gerenciamento de academias de artes marciais, desenvolvida com arquitetura robusta e segurança avançada. A solução oferece uma API REST completa para administração de usuários, modalidades, turmas, aulas e controle de presenças, atendendo às necessidades de academias de todos os portes.

<img width="1611" height="627" alt="Screenshot 2025-09-12 at 20 46 49" src="https://github.com/user-attachments/assets/bf19c9a3-cf42-46bf-8de2-2223f2c4305c" />

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
- [Licença](#licença)

## Características

### Autenticação e Autorização
- **JWT (JSON Web Tokens)** para autenticação segura e stateless
- **Refresh Tokens** para renovação automática de sessões
- **Sistema de Roles** com controle granular de acesso:
  - `ADMIN` - Acesso administrativo completo ao sistema
  - `COORDENADOR` - Gerenciamento de modalidades e professores
  - `PROFESSOR` - Gerenciamento de turmas e aulas
  - `ALUNO` - Acesso às próprias informações e aulas
  - `RESPONSAVEL` - Acompanhamento de alunos menores

### Gerenciamento de Usuários
- Cadastro e autenticação segura de usuários
- Perfis completos com foto e informações pessoais
- Suporte a login social (OAuth2)
- Controle de status ativo/inativo com auditoria

### Gestão de Modalidades
- Cadastro completo de modalidades de artes marciais
- Sistema de especialidades por modalidade
- Controle de graduações e faixas
- Personalização visual com cores e identificadores

### Organização Acadêmica
- **Turmas**: Criação e gerenciamento de turmas por modalidade
- **Aulas**: Agendamento e controle completo de aulas
- **Presenças**: Sistema robusto de controle de frequência
- **Inscrições**: Sistema de inscrições em aulas específicas

### Funcionalidades Administrativas
- Relatórios detalhados de presença e frequência
- Controle de professores por especialidade
- Gerenciamento de responsáveis por alunos
- Histórico completo de matrículas e evolução

## Tecnologias

### Backend
- **Java 21** - Linguagem de programação com recursos modernos
- **Spring Boot 3.2.5** - Framework principal para desenvolvimento
- **Spring Security** - Autenticação e autorização robusta
- **Spring Data JPA** - Persistência de dados com Hibernate
- **Spring Validation** - Validação de dados e constraints

### Banco de Dados
- **PostgreSQL 16** - Banco de dados relacional principal
- **Flyway** - Migração e versionamento do banco de dados
- **H2** - Banco em memória para testes automatizados

### Documentação e Qualidade
- **SpringDoc OpenAPI 3** - Documentação automática da API
- **JaCoCo** - Análise de cobertura de testes
- **Lombok** - Redução de código boilerplate

### Ferramentas de Desenvolvimento
- **Maven** - Gerenciamento de dependências e build
- **Docker Compose** - Containerização do banco de dados
- **JUnit 5** - Framework de testes unitários
- **Mockito** - Framework de mocking para testes

## Pré-requisitos

Antes de começar, certifique-se de ter instalado:

- **Java 21** ou superior
- **Maven 3.6+**
- **PostgreSQL 16** ou superior
- **Docker** e **Docker Compose** (opcional, para usar o banco containerizado)

## Instalação

### 1. Clone o repositório

```bash
git clone https://github.com/seu-usuario/fighthub.git
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

### Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|---------|
| `POSTGRES_DB` | Nome do banco de dados | `fighthub` |
| `POSTGRES_USER` | Usuário do PostgreSQL | `admin` |
| `POSTGRES_PASSWORD` | Senha do PostgreSQL | `111222333` |

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

#### Autenticação
- `POST /auth/login` - Login do usuário
- `POST /auth/refresh` - Renovar token de acesso
- `POST /auth/logout` - Logout do usuário

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

- **Testes Unitários**: Testam componentes isolados
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

### Issues

Encontrou um bug ou tem uma sugestão? Abra uma [issue](https://github.com/seu-usuario/fighthub/issues)!

## Licença

Este projeto está licenciado sob a Licença MIT - veja o arquivo [LICENSE](LICENSE) para detalhes.

---

**FightHub** - Solução empresarial para gerenciamento de academias de artes marciais.

Para mais informações, visite nossa [documentação completa](https://github.com/seu-usuario/fighthub/wiki) ou entre em contato através do email de suporte.
