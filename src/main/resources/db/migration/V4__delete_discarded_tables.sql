-- Tabelas intermediárias (dependentes)
DROP TABLE IF EXISTS alunos_modalidades CASCADE;
DROP TABLE IF EXISTS professores_especialidades CASCADE;
DROP TABLE IF EXISTS inscricao_aula CASCADE;

-- Tabelas principais obsoletas
DROP TABLE IF EXISTS modalidades CASCADE;
DROP TABLE IF EXISTS especialidades CASCADE;