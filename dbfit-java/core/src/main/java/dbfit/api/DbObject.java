package dbfit.api;

import dbfit.util.DbParameterAccessor;
import dbfit.util.Direction;

import java.sql.SQLException;

public interface DbObject {
    public DbCommand buildDbCommand(DbParameterAccessor accessors[]) throws SQLException;
    public DbParameterAccessor getDbParameterAccessor(String paramName, Direction expectedDirection) throws SQLException;
}
