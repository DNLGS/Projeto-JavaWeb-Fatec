package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import models.Funcionario;

public class FuncionarioDao extends PessoaDao<Funcionario> {

    public FuncionarioDao(Connection con) {
        super(con);
    }

    public Funcionario findById(int id) throws SQLException {
        return buscarPessoa("funcionario", id, false, this::map);
    }

    public Funcionario findForUpdate(int id) throws SQLException {
        return buscarPessoa("funcionario", id, true, this::map);
    }

    public List<Funcionario> search(String busca) throws SQLException {
        return pesquisarPessoa("funcionario", busca, this::map);
    }

    private Funcionario map(ResultSet rs) throws SQLException {
        Funcionario funcionario = new Funcionario();
        funcionario.setId(rs.getInt("id"));
        mapCamposPessoa(rs, funcionario);
        funcionario.setCargo(rs.getString("cargo"));
        funcionario.setNivel_acesso(rs.getInt("nivel_acesso"));
        return funcionario;
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO funcionario (nome, documento, endereco_id, dt_nascimento, dt_cadastro, cargo, nivel_acesso) VALUES (?, ?, ?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE funcionario SET nome = ?, documento = ?, endereco_id = ?, dt_nascimento = ?, cargo = ?, nivel_acesso = ? WHERE id = ?";
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
        prepareCamposPessoa(stmt, entity);
        stmt.setString(5, entity.getCargo());
        stmt.setInt(6, entity.getNivel_acesso());
        stmt.setInt(7, entity.getId());
    }

    @Override
    protected void setGeneratedId(Funcionario entity, int id) {
        entity.setId(id);
    }
}
