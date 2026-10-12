package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import models.Funcionario;

public class FuncionarioDao extends PessoaDao<Funcionario> {

    public FuncionarioDao(Connection con) {
        super(con);
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO funcionario (nome, documento, endereco_id, dt_nascimento, dt_cadastro, cargo, nivel_acesso) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE funcionario SET nome = ?, documento = ?, endereco_id = ?, dt_nascimento = ?, dt_cadastro = ?, cargo = ?, nivel_acesso = ? WHERE id = ?";
    }

    @Override
    protected String getDeleteSQL() {
        return "DELETE FROM funcionario WHERE id = ?";
    }

    @Override
    protected void prepareInsert(PreparedStatement stmt, Funcionario entity) throws SQLException {
        preparePessoa(stmt, entity);
        stmt.setString(6, entity.getCargo());
        stmt.setInt(7, entity.getNivel_acesso());
    }

    @Override
    protected void prepareUpdate(PreparedStatement stmt, Funcionario entity) throws SQLException {
        prepareInsert(stmt, entity);
        stmt.setInt(8, entity.getId());
    }

    @Override
    protected void setGeneratedId(Funcionario entity, int id) {
        entity.setId(id);
    }
}
