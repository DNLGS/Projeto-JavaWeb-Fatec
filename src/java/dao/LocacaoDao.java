package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.Objects;

import database.BaseDao;
import models.Cliente;
import models.Filme;
import models.Locacao;

public class LocacaoDao extends BaseDao<Locacao, Integer> {

    public LocacaoDao(Connection con) {
        super(con);
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO locacao (cliente_id, qtd_dias, data_cadastro, data_devolucao) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE locacao SET cliente_id = ?, qtd_dias = ?, data_cadastro = ?, data_devolucao = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM locacao WHERE id = ?";
    }

    @Override
    protected void prepareInsert(PreparedStatement stmt, Locacao entity) throws SQLException {
        stmt.setInt(1, ((Cliente) entity.getCliente()).getId());
        stmt.setInt(2, entity.getQtd_dias());
        setDate(stmt, 3, entity.getData_cadastro());
        setDate(stmt, 4, entity.getData_devolucao());
    }

    @Override
    protected void prepareUpdate(PreparedStatement stmt, Locacao entity) throws SQLException {
        prepareInsert(stmt, entity);
        stmt.setInt(5, entity.getId());
    }

    @Override
    protected void setGeneratedId(Locacao entity, int id) {
        entity.setId(id);
    }

    @Override
    public void insert(Locacao entity) throws SQLException {
        salvar(entity, false);
    }

    @Override
    public void update(Locacao entity) throws SQLException {
        salvar(entity, true);
    }

    private void salvar(Locacao entity, boolean atualizar) throws SQLException {
        Objects.requireNonNull(entity, "A locacao e obrigatoria.");
        if (!(entity.getCliente() instanceof Cliente)
                || ((Cliente) entity.getCliente()).getId() <= 0) {
            throw new SQLException("A locacao precisa de um Cliente salvo.");
        }
        if (entity.getQtd_dias() <= 0 || (atualizar && entity.getId() <= 0)) {
            throw new SQLException("Quantidade de dias e ID para atualizacao devem ser positivos.");
        }
        if (entity.getFilmes() == null || entity.getFilmes().isEmpty()) {
            throw new SQLException("A locacao precisa de pelo menos um filme.");
        }
        for (Filme filme : entity.getFilmes()) {
            if (filme == null || filme.getId() <= 0) {
                throw new SQLException("Todos os filmes precisam estar salvos antes da locacao.");
            }
        }

        boolean transacaoPropria = _con.getAutoCommit();
        int idAnterior = entity.getId();
        Savepoint savepoint = null;
        if (transacaoPropria) {
            _con.setAutoCommit(false);
        } else {
            savepoint = _con.setSavepoint();
        }

        Throwable falha = null;
        boolean transacaoEncerrada = false;
        try {
            if (atualizar) {
                super.update(entity);
                try (PreparedStatement stmt = _con.prepareStatement(
                        "DELETE FROM locacao_filme WHERE locacao_id = ?")) {
                    stmt.setInt(1, entity.getId());
                    stmt.executeUpdate();
                }
            } else {
                super.insert(entity);
            }
            try (PreparedStatement stmt = _con.prepareStatement(
                    "INSERT INTO locacao_filme (locacao_id, filme_id) VALUES (?, ?)")) {
                for (Filme filme : entity.getFilmes()) {
                    stmt.setInt(1, entity.getId());
                    stmt.setInt(2, filme.getId());
                    stmt.addBatch();
                }
                stmt.executeBatch();
            }
            if (transacaoPropria) {
                _con.commit();
            } else {
                _con.releaseSavepoint(savepoint);
            }
            transacaoEncerrada = true;
        } catch (SQLException | RuntimeException ex) {
            falha = ex;
            try {
                if (transacaoPropria) {
                    _con.rollback();
                } else {
                    _con.rollback(savepoint);
                    _con.releaseSavepoint(savepoint);
                }
                transacaoEncerrada = true;
            } catch (SQLException rollbackError) {
                ex.addSuppressed(rollbackError);
            }
            if (!atualizar) {
                entity.setId(idAnterior);
            }
            throw ex;
        } finally {
            if (transacaoPropria && transacaoEncerrada) {
                try {
                    _con.setAutoCommit(true);
                } catch (SQLException restoreError) {
                    if (falha == null) {
                        throw restoreError;
                    }
                    falha.addSuppressed(restoreError);
                }
            }
        }
    }
}
