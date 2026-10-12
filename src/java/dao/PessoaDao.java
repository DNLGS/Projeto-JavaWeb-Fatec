package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;

import database.BaseDao;
import models.Endereco;
import models.Pessoa;

public abstract class PessoaDao<T extends Pessoa> extends BaseDao<T, Integer> {

    protected PessoaDao(Connection con) {
        super(con);
    }

    protected void preparePessoa(PreparedStatement stmt, T entity) throws SQLException {
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
        setDate(stmt, 5, entity.getDt_cadastro());
    }
}
