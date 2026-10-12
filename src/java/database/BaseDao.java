package database;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.Objects;

public abstract class BaseDao<T, ID> {

    protected final Connection _con;

    public BaseDao(Connection con) {
        this._con = Objects.requireNonNull(con, "A conexao e obrigatoria.");
    }

    protected abstract String getInsertSQL();
    protected abstract String getUpdateSQL();
    protected abstract String getDeleteSQL();
    protected abstract void prepareInsert(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void prepareUpdate(PreparedStatement stmt, T entity) throws SQLException;
    protected abstract void setGeneratedId(T entity, int id);

    public void insert(T entity) throws SQLException {
        Objects.requireNonNull(entity, "A entidade e obrigatoria.");
        try (PreparedStatement stmt = _con.prepareStatement(
                getInsertSQL(), Statement.RETURN_GENERATED_KEYS)) {
            prepareInsert(stmt, entity);
            stmt.executeUpdate();
            try (ResultSet keys = stmt.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("O banco nao retornou o ID gerado.");
                }
                setGeneratedId(entity, keys.getInt(1));
            }
        }
    }

    public void update(T entity) throws SQLException {
        Objects.requireNonNull(entity, "A entidade e obrigatoria.");
        try (PreparedStatement stmt = _con.prepareStatement(getUpdateSQL())) {
            prepareUpdate(stmt, entity);
            stmt.executeUpdate();
        }
    }

    public void delete(ID id) throws SQLException {
        Objects.requireNonNull(id, "O ID e obrigatorio.");
        try (PreparedStatement stmt = _con.prepareStatement(getDeleteSQL())) {
            stmt.setObject(1, id);
            stmt.executeUpdate();
        }
    }

    protected static void setDate(PreparedStatement stmt, int index, LocalDate value)
            throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.DATE);
        } else {
            stmt.setDate(index, Date.valueOf(value));
        }
    }
}
