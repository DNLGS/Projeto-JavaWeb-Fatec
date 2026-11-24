package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.BaseDao;
import models.Endereco;

public class EnderecoDao extends BaseDao<Endereco, Integer> {

    public EnderecoDao(Connection con) {
        super(con);
    }

    public Endereco findById(int id) throws SQLException {
        try (PreparedStatement stmt = _con.prepareStatement("SELECT * FROM endereco WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Endereco> search(String busca) throws SQLException {
        String sql = "SELECT * FROM endereco WHERE CAST(id AS CHAR) LIKE ? OR logradouro LIKE ? "
                + "OR bairro LIKE ? OR LPAD(cep, 8, '0') LIKE ? ORDER BY id DESC";
        List<Endereco> enderecos = new ArrayList<>();
        try (PreparedStatement stmt = _con.prepareStatement(sql)) {
            String filtro = "%" + busca + "%";
            for (int i = 1; i <= 4; i++) {
                stmt.setString(i, filtro);
            }
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    enderecos.add(map(rs));
                }
            }
        }
        return enderecos;
    }

    private Endereco map(ResultSet rs) throws SQLException {
        Endereco endereco = new Endereco();
        endereco.setId(rs.getInt("id"));
        endereco.setLogradouro(rs.getString("logradouro"));
        endereco.setBairro(rs.getString("bairro"));
        endereco.setNumero(rs.getString("numero"));
        endereco.setIbge(rs.getInt("ibge"));
        endereco.setCep(rs.getInt("cep"));
        return endereco;
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO endereco (logradouro, bairro, numero, ibge, cep) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE endereco SET logradouro = ?, bairro = ?, numero = ?, ibge = ?, cep = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM endereco WHERE id = ?";
    }

    @Override
    protected void prepareInsert(PreparedStatement stmt, Endereco entity) throws SQLException {
        stmt.setString(1, entity.getLogradouro());
        stmt.setString(2, entity.getBairro());
        stmt.setString(3, entity.getNumero());
        stmt.setInt(4, entity.getIbge());
        stmt.setInt(5, entity.getCep());
    }

    @Override
    protected void prepareUpdate(PreparedStatement stmt, Endereco entity) throws SQLException {
        prepareInsert(stmt, entity);
        stmt.setInt(6, entity.getId());
    }

    @Override
    protected void setGeneratedId(Endereco entity, int id) {
        entity.setId(id);
    }
}
