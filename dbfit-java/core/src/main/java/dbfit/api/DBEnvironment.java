package dbfit.api;

import dbfit.util.DbParameterAccessor;
import dbfit.util.NameNormaliser;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Map;


public interface DBEnvironment {
    /**
     * Meta-data retrieval method that provides a list of parameters for a given
     * stored procedure or function name. The name may contain a schema
     * qualifier.
     * 
     * While implementing, use {@link NameNormaliser} to make sure parameters
     * are mapped properly.
     * 
     * Parameters that map to return values should have an empty string for the
     * name.
     */
    Map<String, DbParameterAccessor> getAllProcedureParameters(String procName)
            throws SQLException;

    Map<String, DbParameterAccessor> getAllProcedureParameters(String procName, String params)
            throws SQLException;
    /**
     * Meta-data retrieval method that provides a list of columns a given stored
     * table or view. The name may contain a schema qualifier.
     */
    Map<String, DbParameterAccessor> getAllColumns(String tableOrViewName)
            throws SQLException;

    /**
     * This method creates an insert command that will be used to populate new
     * rows in a table.
     */
    PreparedStatement buildInsertPreparedStatement(String tableName, DbParameterAccessor[] accessors)
            throws SQLException;

    /**
     * This method should convert the statement parameter accessor to an accessor
     * that can retrieve the autogenerated primary key value of the field described 
     * by the accessor after insert. it is abstracted here to allow different database
     * environment implementations to provide a db-specific way of retrieving primary keys.
     *
     * You normally want to use DbAutoGeneratedKeyAccessor here
     */
    DbParameterAccessor createAutogeneratedPrimaryKeyAccessor(DbParameterAccessor template); 
  
    /*
     * CreateCommand(String statement) and BindFixtureSymbols are implemented
     * differently then in the .Net version due to JDBC API; they are combined
     * into createStatementWithBoundFixtureSymbols
     */

    /**
     * Create a {@link PreparedStatement} object and binds fixture symbols to
     * SQL statement parameters with matching names.
     */
    PreparedStatement createStatementWithBoundFixtureSymbols(TestHost th, String commandText)
            throws SQLException;

    /**
     * Closes the current connection and rolls back any active transactions. The
     * transactions are automatically rolled back to make tests repeatable.
     */
    void closeConnection() throws SQLException;

    /**
     * Connects to the database using a default database for the user.
     * 
     * @param dataSource
     *            Host (optionally port), machine name or any other data source
     *            identifier
     */
    void connect(String dataSource, String username, String password)
            throws SQLException;

    /**
     * Connects to the database using a specified database.
     * 
     * @param dataSource
     *            Host (optionally port), machine name or any other data source
     *            identifier
     * @param database
     *            Database to use after connecting
     */
    void connect(String dataSource, String username, String password,
            String database) throws SQLException;

    /**
     * Connects using a database-specific connection string. This allows users
     * to specify parameters that would not be used otherwise (i.e. windows
     * integrated security or a different network protocol).
     * 
     * @param connectionString
     *            full JDBC connection string
     */
    void connect(String connectionString) throws SQLException;

    /**
     * Commit current transaction.
     */
    void commit() throws SQLException;

    /**
     * Rollback current transaction.
     */
    void rollback() throws SQLException;

    /**
     * Retrieve an exception code from a database exception. This method should
     * perform any required conversion between a JDBC exception and the real
     * database error code.
     */
    int getExceptionCode(SQLException ex);

    /**
     * Retrieve current connection. Could be used by 3rd party classes to
     * execute database commands in the same session.
     */
    Connection getConnection() throws SQLException;

    /**
     * Get the Java class that should be used to store objects of a DB specific
     * data type.
     * 
     * @param dataType
     *            DB data type name
     */
    Class<?> getJavaClass(String dataType);

    /**
     * Load database properties from a file and connect. The connection
     * properties file is a plain text file, containing key/value pairs
     * separarted by the equals symbol (=). Lines starting with a hash (#) are
     * ignored. Use the following keys (they care case-sensitive):
     * 
     * 1. service -- service name. In the previous example, it was
     * LAPTOP\SQLEXPRESS. 2. username -- username to connect to the database. In
     * the previous example, it was FitNesseUser. 3. password -- password to
     * connect to the database. In the previous example, it was Password. 4.
     * database -- optional fourth argument, allowing you to choose the active
     * database. In the previous example, it was TestDB. 5. connection-string --
     * alternative to the four previous parameters, this allows you to specify
     * the full connection string. This parameter should not be mixed with any
     * of the four other keys. Use either the full string or specify individual
     * properties.
     * 
     * <pre>
     * Here is an example:
     *  
     *  # DBFit connection properties file
     *  #
     *  #1) Either specify full connection string
     *  #connection-string=
     *  #
     *  #2) OR specify service, username and password as separate properties
     *  service=localhost
     *  username=root
     *  password=
     *  #optionally specify a database name
     *  database=dbfit
     * </pre>
     * 
     * @param filePath
     *            path to the configuration file
     */
    void connectUsingFile(String filePath) throws SQLException, IOException,
            FileNotFoundException;

    DbStoredProcedureCall newStoredProcedureCall(String name, DbParameterAccessor[] accessors);
}

