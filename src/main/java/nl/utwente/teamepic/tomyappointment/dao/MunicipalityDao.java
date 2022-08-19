package nl.utwente.teamepic.tomyappointment.dao;

import nl.utwente.teamepic.tomyappointment.database.DatabaseConnection;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import nl.utwente.teamepic.tomyappointment.model.Theme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MunicipalityDao extends Dao<Municipality> {
    private final static String TABLE_NAME = "teamepic.municipality";
    private final Connection connection;

    public MunicipalityDao() throws SQLException, ClassNotFoundException {
        this.connection = DatabaseConnection.getConnection();
    }

    @Override
    public List<Municipality> handleStatement(PreparedStatement statement) throws SQLException {
        ResultSet results = statement.executeQuery();
        List<Municipality> municipalities = new ArrayList<>();

        while (results.next()) {
            municipalities.add(new Municipality(
                    results.getString(Columns.ID),
                    results.getString(Columns.NAME),
                    results.getString(Columns.SHORT_NAME)
            ));
        }

        results.close();
        statement.close();

        return municipalities;
    }

    @Override
    public List<Municipality> getAll() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from " + TABLE_NAME);

        return handleStatement(statement);
    }

    public List<Municipality> getById(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from " + TABLE_NAME + " where id = ?");
        statement.setString(1, id);

        return handleStatement(statement);
    }

    @Override
    public String insert(Municipality municipality) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "insert into " + TABLE_NAME + " (id, name, short_name) values (?, ?, ?) returning id"
        );
        statement.setString(1, municipality.getID());
        statement.setString(2, municipality.getName());
        statement.setString(3, municipality.getShortName());

        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String insertedId = resultSet.getString(1);

        statement.close();
        resultSet.close();

        return insertedId;
    }

    @Override
    public int update(Municipality municipality) throws SQLException {
        HashMap<String, String> columnsValues = new HashMap<>();

        columnsValues.put(Columns.NAME, municipality.getName());
        columnsValues.put(Columns.SHORT_NAME, municipality.getShortName());

        StringBuilder sql = new StringBuilder("update " + TABLE_NAME + " set");

        for (Map.Entry<String, String> pair : columnsValues.entrySet()) {
            if (pair.getValue() != null) {
                if (sql.toString().endsWith("?")) sql.append(",");
                sql.append(" ").append(pair.getKey()).append(" = ?");
            }
        }

        sql.append(" where id = ?");


        PreparedStatement statement = connection.prepareStatement(sql.toString());

        int tempCount = 1;
        for (String value : columnsValues.values()) {
            if (value != null) {
                statement.setString(tempCount, value);
                tempCount += 1;
            }
        }

        statement.setString(tempCount, municipality.getID());

        int rowsUpdated = statement.executeUpdate();
        statement.close();

        return rowsUpdated;
    }

    @Override
    public int delete(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("delete from " + TABLE_NAME + " where id = ?");
        statement.setString(1, id);

        int rowsDeleted = statement.executeUpdate();
        statement.close();

        return rowsDeleted;
    }

    public static class Columns {
        public static final String ID = "id";
        public static final String NAME = "name";
        public static final String SHORT_NAME = "short_name";
    }
}