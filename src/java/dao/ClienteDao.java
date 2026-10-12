package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import models.Cliente;

public class ClienteDao extends PessoaDao<Cliente> {

    public ClienteDao(Connection con) {
        super(con);
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cliente (nome, documento, endereco_id, dt_nascimento, dt_cadastro) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE cliente SET nome = ?, documento = ?, endereco_id = ?, dt_nascimento = ?, dt_cadastro = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM cliente WHERE id = ?";
    }

    @Override
    protected void prepareInsert(PreparedStatement stmt, Cliente entity) throws SQLException {
        preparePessoa(stmt, entity);
    }

    @Override
    protected void prepareUpdate(PreparedStatement stmt, Cliente entity) throws SQLException {
        preparePessoa(stmt, entity);
        stmt.setInt(6, entity.getId());
    }

    @Override
    protected void setGeneratedId(Cliente entity, int id) {
        entity.setId(id);
    }
}
