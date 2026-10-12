package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import database.BaseDao;
import models.Endereco;

public class EnderecoDao extends BaseDao<Endereco, Integer> {

    public EnderecoDao(Connection con) {
        super(con);
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
