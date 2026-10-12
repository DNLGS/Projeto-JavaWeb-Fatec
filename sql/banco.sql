CREATE DATABASE IF NOT EXISTS `projeto-jean-2026`
    CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE `projeto-jean-2026`;

CREATE TABLE IF NOT EXISTS endereco (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    logradouro VARCHAR(255) NOT NULL,
    bairro VARCHAR(150),
    numero VARCHAR(30),
    ibge INT NOT NULL CHECK (ibge BETWEEN 0 AND 9999999),
    cep INT NOT NULL CHECK (cep BETWEEN 0 AND 99999999)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS cliente (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    endereco_id INT,
    dt_nascimento DATE,
    dt_cadastro DATE NOT NULL,
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS funcionario (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(200) NOT NULL,
    documento VARCHAR(30) NOT NULL UNIQUE,
    endereco_id INT,
    dt_nascimento DATE,
    dt_cadastro DATE NOT NULL,
    cargo VARCHAR(100),
    nivel_acesso INT NOT NULL DEFAULT 0 CHECK (nivel_acesso >= 0),
    FOREIGN KEY (endereco_id) REFERENCES endereco(id)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS filme (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    titulo VARCHAR(255) NOT NULL,
    genero VARCHAR(100),
    nota_imb DECIMAL(3,1) NOT NULL CHECK (nota_imb BETWEEN 0 AND 10),
    observacoes TEXT,
    valor_dia DECIMAL(12,2) NOT NULL CHECK (valor_dia >= 0)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS locacao (
    id INT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    cliente_id INT NOT NULL,
    qtd_dias INT NOT NULL CHECK (qtd_dias > 0),
    data_cadastro DATE NOT NULL,
    data_devolucao DATE,
    FOREIGN KEY (cliente_id) REFERENCES cliente(id),
    CHECK (data_devolucao IS NULL OR data_devolucao >= data_cadastro)
) ENGINE=InnoDB;

CREATE TABLE IF NOT EXISTS locacao_filme (
    locacao_id INT NOT NULL,
    filme_id INT NOT NULL,
    PRIMARY KEY (locacao_id, filme_id),
    FOREIGN KEY (locacao_id) REFERENCES locacao(id) ON DELETE CASCADE,
    FOREIGN KEY (filme_id) REFERENCES filme(id)
) ENGINE=InnoDB;
