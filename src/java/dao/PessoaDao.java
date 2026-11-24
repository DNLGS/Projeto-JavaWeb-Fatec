package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import database.BaseDao;
import models.Endereco;
import models.Pessoa;

public abstract class PessoaDao<T extends Pessoa> extends BaseDao<T, Integer> {

    protected PessoaDao(Connection con) {
        super(con);
    }

    protected void preparePessoa(PreparedStatement stmt, T entity) throws SQLException {
        prepareCamposPessoa(stmt, entity);
        setDate(stmt, 5, entity.getDt_cadastro());
    }

    protected void prepareCamposPessoa(PreparedStatement stmt, T entity) throws SQLException {
        stmt.setString(1, entity.getNome());
        stmt.setString(2, entity.getDocumento());
        Endereco endereco = entity.getEndereco();
        if (endereco == null) {
            stmt.setNull(3, Types.INTEGER);
        } else {
            if (endereco.getId() <= 0) {
                throw new SQLException("Salve o endereco antes de adicionar na pessoa.");
            }
            stmt.setInt(3, endereco.getId());
        }
        setDate(stmt, 4, entity.getDt_nascimento());
    }

    @FunctionalInterface
    protected interface PessoaMapper<P> {
        P map(ResultSet rs) throws SQLException;
    }

    protected T buscarPessoa(String tabela, int id, boolean bloquear, PessoaMapper<T> mapper) throws SQLException {
        String sql = consulta(tabela) + " WHERE p.id = ?" + (bloquear ? " FOR UPDATE" : "");
        try (PreparedStatement stmt = _con.prepareStatement(sql)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? mapper.map(rs) : null;
            }
        }
    }

    protected List<T> pesquisarPessoa(String tabela, String busca, PessoaMapper<T> mapper) throws SQLException {
        String sql = consulta(tabela) + " WHERE CONCAT(p.nome, ' ', p.documento) LIKE ? ORDER BY p.id DESC";
        List<T> pessoas = new ArrayList<>();
        try (PreparedStatement stmt = _con.prepareStatement(sql)) {
            stmt.setString(1, "%" + busca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    pessoas.add(mapper.map(rs));
                }
            }
        }
        return pessoas;
    }

    private String consulta(String tabela) {
        return "SELECT p.*, e.logradouro, e.numero FROM " + tabela
                + " p LEFT JOIN endereco e ON e.id = p.endereco_id";
    }

    protected void mapCamposPessoa(ResultSet rs, T pessoa) throws SQLException {
        pessoa.setNome(rs.getString("nome"));
        pessoa.setDocumento(rs.getString("documento"));
        pessoa.setDt_nascimento(rs.getObject("dt_nascimento", LocalDate.class));
        pessoa.setDt_cadastro(rs.getObject("dt_cadastro", LocalDate.class));
        int enderecoId = rs.getInt("endereco_id");
        if (!rs.wasNull()) {
            Endereco endereco = new Endereco();
            endereco.setId(enderecoId);
            endereco.setLogradouro(rs.getString("logradouro"));
            endereco.setNumero(rs.getString("numero"));
            pessoa.setEndereco(endereco);
        }
    }
}
