package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import database.BaseDao;
import models.Filme;

public class FilmeDao extends BaseDao<Filme, Integer> {

    public FilmeDao(Connection con) {
        super(con);
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
