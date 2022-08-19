package nl.utwente.teamepic.tomyappointment.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

abstract class Dao<T> {
    abstract List<T> handleStatement(PreparedStatement statement) throws SQLException;

    abstract List<T> getAll() throws SQLException;

    abstract List<T> getById(String id) throws SQLException;

    String insert(T object) throws SQLException {
        return null;
    }

    int update(T object) throws SQLException {
        return -1;
    }

    int delete(String id) throws SQLException {
        return -1;
    }
}
