package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import database.BaseDao;
import models.Cliente;
import models.Filme;
import models.Locacao;

public class LocacaoDao extends BaseDao<Locacao, Integer> {
    private static final String CONSULTA = "SELECT l.id, l.cliente_id, c.nome, l.qtd_dias, "
            + "l.data_cadastro, l.data_devolucao, f.id AS filme_id, f.titulo "
            + "FROM locacao l JOIN cliente c ON c.id = l.cliente_id "
            + "LEFT JOIN locacao_filme lf ON lf.locacao_id = l.id LEFT JOIN filme f ON f.id = lf.filme_id";

    public LocacaoDao(Connection con) {
        super(con);
    }

    public Locacao findById(int id) throws SQLException {
        try (PreparedStatement stmt = _con.prepareStatement(CONSULTA + " WHERE l.id = ? ORDER BY f.id")) {
            stmt.setInt(1, id);
            List<Locacao> locacoes = carregar(stmt);
            return locacoes.isEmpty() ? null : locacoes.getFirst();
        }
    }

    public List<Locacao> search(String busca) throws SQLException {
        String sql = CONSULTA + " WHERE c.nome LIKE ? OR EXISTS (SELECT 1 FROM locacao_filme pesquisa "
                + "JOIN filme pf ON pf.id = pesquisa.filme_id WHERE pesquisa.locacao_id = l.id AND pf.titulo LIKE ?) "
                + "ORDER BY l.id DESC, f.id";
        try (PreparedStatement stmt = _con.prepareStatement(sql)) {
            String filtro = "%" + busca + "%";
            stmt.setString(1, filtro);
            stmt.setString(2, filtro);
            return carregar(stmt);
        }
    }

    private List<Locacao> carregar(PreparedStatement stmt) throws SQLException {
        Map<Integer, Locacao> locacoes = new LinkedHashMap<>();
        try (ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("id");
                Locacao locacao = locacoes.get(id);
                if (locacao == null) {
                    locacao = new Locacao(rs.getObject("data_cadastro", LocalDate.class));
                    locacao.setId(id);
                    locacao.setQtd_dias(rs.getInt("qtd_dias"));
                    locacao.setData_devolucao(rs.getObject("data_devolucao", LocalDate.class));
                    Cliente cliente = new Cliente();
                    cliente.setId(rs.getInt("cliente_id"));
                    cliente.setNome(rs.getString("nome"));
                    locacao.setCliente(cliente);
                    locacoes.put(id, locacao);
                }
                int filmeId = rs.getInt("filme_id");
                if (!rs.wasNull()) {
                    Filme filme = new Filme();
                    filme.setId(filmeId);
                    filme.setTitulo(rs.getString("titulo"));
                    locacao.addFilme(filme);
                }
            }
        }
        return new ArrayList<>(locacoes.values());
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO locacao (cliente_id, qtd_dias, data_cadastro, data_devolucao) VALUES (?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE locacao SET cliente_id = ?, qtd_dias = ?, data_devolucao = ? WHERE id = ?";
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
        stmt.setInt(1, ((Cliente) entity.getCliente()).getId());
        stmt.setInt(2, entity.getQtd_dias());
        setDate(stmt, 3, entity.getData_devolucao());
        stmt.setInt(4, entity.getId());
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
    public int update(Locacao entity) throws SQLException {
        return salvar(entity, true);
    }

    private int salvar(Locacao entity, boolean atualizar) throws SQLException {
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
        int alterados = 1;
        try {
            if (atualizar) {
                alterados = super.update(entity);
                if (alterados == 0 && !existeParaAtualizar(entity.getId())) {
                    if (transacaoPropria) {
                        _con.rollback();
                    } else {
                        _con.rollback(savepoint);
                        _con.releaseSavepoint(savepoint);
                    }
                    transacaoEncerrada = true;
                    return 0;
                }
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
            return alterados;
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

    private boolean existeParaAtualizar(int id) throws SQLException {
        try (PreparedStatement stmt = _con.prepareStatement("SELECT id FROM locacao WHERE id = ? FOR UPDATE")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        }
    }
}
