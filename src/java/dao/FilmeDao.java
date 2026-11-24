package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import database.BaseDao;
import models.Filme;

public class FilmeDao extends BaseDao<Filme, Integer> {

    public FilmeDao(Connection con) {
        super(con);
    }

    public Filme findById(int id) throws SQLException {
        try (PreparedStatement stmt = _con.prepareStatement("SELECT * FROM filme WHERE id = ?")) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Filme> search(String busca) throws SQLException {
        String sql = "SELECT * FROM filme WHERE CONCAT(titulo, ' ', COALESCE(genero, '')) LIKE ? ORDER BY id DESC";
        List<Filme> filmes = new ArrayList<>();
        try (PreparedStatement stmt = _con.prepareStatement(sql)) {
            stmt.setString(1, "%" + busca + "%");
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    filmes.add(map(rs));
                }
            }
        }
        return filmes;
    }

    private Filme map(ResultSet rs) throws SQLException {
        Filme filme = new Filme();
        filme.setId(rs.getInt("id"));
        filme.setTitulo(rs.getString("titulo"));
        filme.setGenero(rs.getString("genero"));
        filme.setNota_imb(rs.getDouble("nota_imb"));
        filme.setValor_dia(rs.getBigDecimal("valor_dia").doubleValue());
        filme.setObservacoes(rs.getString("observacoes"));
        return filme;
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO filme (titulo, genero, nota_imb, observacoes, valor_dia) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE filme SET titulo = ?, genero = ?, nota_imb = ?, observacoes = ?, valor_dia = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM filme WHERE id = ?";
    }

    @Override
    protected void prepareInsert(PreparedStatement stmt, Filme entity) throws SQLException {
        if (!Double.isFinite(entity.getValor_dia()) || entity.getValor_dia() < 0
                || !Double.isFinite(entity.getNota_imb())
                || entity.getNota_imb() < 0 || entity.getNota_imb() > 10) {
            throw new SQLException("Valor diario deve ser maior que zero; nota deve estar entre 0 e 10.");
        }
        stmt.setString(1, entity.getTitulo());
        stmt.setString(2, entity.getGenero());
        stmt.setDouble(3, entity.getNota_imb());
        stmt.setString(4, entity.getObservacoes());
        stmt.setBigDecimal(5, java.math.BigDecimal.valueOf(entity.getValor_dia()));
    }

    @Override
    protected void prepareUpdate(PreparedStatement stmt, Filme entity) throws SQLException {
        prepareInsert(stmt, entity);
        stmt.setInt(6, entity.getId());
    }

    @Override
    protected void setGeneratedId(Filme entity, int id) {
        entity.setId(id);
    }
}
