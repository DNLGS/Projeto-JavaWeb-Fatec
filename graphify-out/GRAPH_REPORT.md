# Graph Report - Projeto-JavaWeb-Fatec  (2026-10-13)

## Corpus Check
- 16 files · ~4,539 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 178 nodes · 415 edges · 8 communities
- Extraction: 85% EXTRACTED · 15% INFERRED · 0% AMBIGUOUS · INFERRED: 64 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `1be48c83`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- Endereco
- Locacao
- .doPost
- ClassServletBase.java
- Filme
- Funcionario
- Cliente
- BaseDao

## God Nodes (most connected - your core abstractions)
1. `Locacao` - 24 edges
2. `Endereco` - 22 edges
3. `Filme` - 22 edges
4. `BaseDao` - 21 edges
5. `Pessoa` - 19 edges
6. `ClassServletBase` - 16 edges
7. `Funcionario` - 15 edges
8. `LocacaoDao` - 13 edges
9. `Cliente` - 12 edges
10. `ClienteDao` - 10 edges

## Surprising Connections (you probably didn't know these)
- `ClienteDao` --inherits--> `PessoaDao`  [EXTRACTED]
  src/java/dao/ClienteDao.java → src/java/dao/PessoaDao.java
- `EnderecoDao` --inherits--> `BaseDao`  [EXTRACTED]
  src/java/dao/EnderecoDao.java → src/java/database/BaseDao.java
- `FilmeDao` --inherits--> `BaseDao`  [EXTRACTED]
  src/java/dao/FilmeDao.java → src/java/database/BaseDao.java
- `FuncionarioDao` --inherits--> `PessoaDao`  [EXTRACTED]
  src/java/dao/FuncionarioDao.java → src/java/dao/PessoaDao.java
- `LocacaoDao` --inherits--> `BaseDao`  [EXTRACTED]
  src/java/dao/LocacaoDao.java → src/java/database/BaseDao.java

## Import Cycles
- None detected.

## Communities (8 total, 0 thin omitted)

### Community 0 - "Endereco"
Cohesion: 0.10
Nodes (6): EnderecoDao, Connection, Override, PreparedStatement, Endereco, Pessoa

### Community 1 - "Locacao"
Cohesion: 0.17
Nodes (5): Connection, Override, PreparedStatement, LocacaoDao, Locacao

### Community 2 - ".doPost"
Cohesion: 0.21
Nodes (4): HttpServletRequest, HttpServletResponse, ClassServletBase, Override

### Community 3 - "ClassServletBase.java"
Cohesion: 0.16
Nodes (9): HttpServlet, SQLException, Connection, Connection, PreparedStatement, PessoaDao, Database, Connection (+1 more)

### Community 4 - "Filme"
Cohesion: 0.21
Nodes (5): FilmeDao, Connection, Override, PreparedStatement, Filme

### Community 5 - "Funcionario"
Cohesion: 0.19
Nodes (5): FuncionarioDao, Connection, Override, PreparedStatement, Funcionario

### Community 6 - "Cliente"
Cohesion: 0.24
Nodes (5): ClienteDao, Connection, Override, PreparedStatement, Cliente

### Community 7 - "BaseDao"
Cohesion: 0.24
Nodes (3): BaseDao, Connection, PreparedStatement

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `BaseDao` connect `BaseDao` to `Endereco`, `Locacao`, `ClassServletBase.java`, `Filme`?**
  _High betweenness centrality (0.180) - this node is a cross-community bridge._
- **Why does `Pessoa` connect `Endereco` to `Locacao`, `ClassServletBase.java`, `Funcionario`, `Cliente`?**
  _High betweenness centrality (0.165) - this node is a cross-community bridge._
- **Why does `Locacao` connect `Locacao` to `Endereco`, `.doPost`, `ClassServletBase.java`, `Filme`?**
  _High betweenness centrality (0.153) - this node is a cross-community bridge._
- **Should `Endereco` be split into smaller, more focused modules?**
  _Cohesion score 0.09815078236130868 - nodes in this community are weakly interconnected._