package nl.utwente.teamepic.tomyappointment.dao;

import nl.utwente.teamepic.tomyappointment.database.DatabaseConnection;
import nl.utwente.teamepic.tomyappointment.model.Theme;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ThemeDao extends Dao<Theme> {
    private final static String TABLE_NAME = "teamepic.theme";
    private final static String UUID_CAST = "::uuid";
    private final static String BASE_QUERY = TABLE_NAME + " where " + Columns.MUNICIPALITY_ID + " = ?";
    private final static String ID_QUERY = BASE_QUERY + " and " + Columns.ID + " = ?" + UUID_CAST;
    private final Connection connection;
    private final String municipalityId;

    public ThemeDao(String municipalityId) throws SQLException, ClassNotFoundException {
        this.connection = DatabaseConnection.getConnection();
        this.municipalityId = municipalityId;
    }

    public ThemeDao() throws SQLException, ClassNotFoundException {
        this.connection = DatabaseConnection.getConnection();
        this.municipalityId = null;
    }

    public List<Theme> handleStatement(PreparedStatement statement) throws SQLException {
        ResultSet results = statement.executeQuery();
        List<Theme> themes = new ArrayList<>();

        while (results.next()) {
            themes.add(new Theme(
                    results.getString(Columns.ID),
                    results.getString(Columns.MUNICIPALITY_ID),
                    results.getString(Columns.LOCATION),
                    results.getString(Columns.COLOUR),
                    results.getString(Columns.HOME_URL),
                    results.getString(Columns.FONT_URL),
                    results.getString(Columns.FONT_FAMILY),
                    results.getBoolean(Columns.IS_DEFAULT)
            ));
        }

        results.close();
        statement.close();

        return themes;
    }

    public List<Theme> getAllOfAll() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from " + TABLE_NAME);

        return handleStatement(statement);
    }

    public List<Theme> getAll() throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from " + BASE_QUERY);
        statement.setString(1, municipalityId);

        return handleStatement(statement);
    }

    public List<Theme> getById(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from " + ID_QUERY);
        statement.setString(1, municipalityId);
        statement.setString(2, id);

        return handleStatement(statement);
    }

    @Override
    public String insert(Theme theme) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(
                "insert into " + TABLE_NAME + " (id, municipality_id, location, colour, home_url, font_url, font_family, is_default)" +
                        "values (?" + UUID_CAST + ", ?, ?, ?, ?, ?, ?, ?) returning id"
        );
        statement.setString(1, theme.getID());
        statement.setString(2, municipalityId);
        statement.setString(3, theme.getLocation());
        statement.setString(4, theme.getColour());
        statement.setString(5, theme.getHome_url());
        statement.setString(6, theme.getFont_url());
        statement.setString(7, theme.getFont_family());
        statement.setBoolean(8, theme.isIs_default());


        ResultSet resultSet = statement.executeQuery();
        resultSet.next();
        String insertedId = resultSet.getString(1);

        statement.close();
        resultSet.close();

        return insertedId;
    }

    @Override
    public int update(Theme theme) throws SQLException {
        HashMap<String, String> columnsValues = new HashMap<>();

        columnsValues.put(Columns.LOCATION, theme.getLocation());
        columnsValues.put(Columns.COLOUR, theme.getColour());
        columnsValues.put(Columns.HOME_URL, theme.getHome_url());
        columnsValues.put(Columns.FONT_URL, theme.getFont_url());
        columnsValues.put(Columns.FONT_FAMILY, theme.getFont_family());

        StringBuilder sql = new StringBuilder("update " + TABLE_NAME + " set");

        for (Map.Entry<String, String> pair : columnsValues.entrySet()) {
            if (pair.getValue() != null) {
                if (sql.toString().endsWith("?")) sql.append(",");
                sql.append(" ").append(pair.getKey()).append(" = ?");
            }
        }
        if (theme.isIs_default() != null) {
            if (sql.toString().endsWith("?")) sql.append(",");
            sql.append(" ").append(Columns.IS_DEFAULT).append(" = ?");
        }

        sql.append(" where " + Columns.MUNICIPALITY_ID + " = ? and ").append(Columns.ID + " = ?").append(UUID_CAST);

        PreparedStatement statement = connection.prepareStatement(sql.toString());

        int tempCount = 1;
        for (String value : columnsValues.values()) {
            if (value != null) {
                statement.setString(tempCount, value);
                tempCount += 1;
            }
        }

        if (theme.isIs_default() != null) {
            statement.setBoolean(tempCount, theme.isIs_default());
            tempCount += 1;
        }

        statement.setString(tempCount, theme.getMunicipality_id());
        tempCount += 1;
        statement.setString(tempCount, theme.getID());

        int rowsUpdated = statement.executeUpdate();
        statement.close();

        return rowsUpdated;
    }

    @Override
    public int delete(String id) throws SQLException {
        PreparedStatement statement = connection.prepareStatement("delete from " + ID_QUERY);
        statement.setString(1, municipalityId);
        statement.setString(2, id);

        int rowsDeleted = statement.executeUpdate();
        statement.close();

        return rowsDeleted;
    }

    public static class Columns {
        public static final String ID = "id";
        public static final String MUNICIPALITY_ID = "municipality_id";
        public static final String LOCATION = "location";
        public static final String COLOUR = "colour";
        public static final String HOME_URL = "home_url";
//        public static final String START_URL = "start_url";
        public static final String FONT_FAMILY = "font_family";
        public static final String FONT_URL = "font_url";
        public static final String IS_DEFAULT = "is_default";

        public static final String BACKGROUND = "background";
        public static final String LOGO = "logo";
        public static final String ICON_512 = "icon-512";
        public static final String ICON_192 = "icon-192";
        public static final String FONT_FILE = "font";
    }
}
