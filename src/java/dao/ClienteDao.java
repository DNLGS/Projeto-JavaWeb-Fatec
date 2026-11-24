package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import models.Cliente;

public class ClienteDao extends PessoaDao<Cliente> {

    public ClienteDao(Connection con) {
        super(con);
    }

    public Cliente findById(int id) throws SQLException {
        return buscarPessoa("cliente", id, false, this::map);
    }

    public Cliente findForUpdate(int id) throws SQLException {
        return buscarPessoa("cliente", id, true, this::map);
    }

    public List<Cliente> search(String busca) throws SQLException {
        return pesquisarPessoa("cliente", busca, this::map);
    }

    private Cliente map(ResultSet rs) throws SQLException {
        Cliente cliente = new Cliente();
        cliente.setId(rs.getInt("id"));
        mapCamposPessoa(rs, cliente);
        return cliente;
    }

    @Override
    protected String getInsertSQL() {
        return "INSERT INTO cliente (nome, documento, endereco_id, dt_nascimento, dt_cadastro) VALUES (?, ?, ?, ?, ?)";
    }

    @Override
    protected String getUpdateSQL() {
        return "UPDATE cliente SET nome = ?, documento = ?, endereco_id = ?, dt_nascimento = ? WHERE id = ?";
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
        prepareCamposPessoa(stmt, entity);
        stmt.setInt(5, entity.getId());
    }

    @Override
    protected void setGeneratedId(Cliente entity, int id) {
        entity.setId(id);
    }
}
