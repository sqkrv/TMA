package nl.utwente.teamepic.tomyappointment.exceptions;

import java.sql.SQLException;

public class SQLCallException extends GeneralException {
    private final static String MESSAGE = "Server has encountered an error during database query execution";

    public SQLCallException() {
        super(MESSAGE);
    }

    public SQLCallException(String message) {
        super(message);
    }

    public SQLCallException(SQLException exception) {
        super(MESSAGE + ":\n\n" + exception.getMessage());
    }
}
