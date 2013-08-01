package dbfit.fixture;

import dbfit.api.DbObject;
import dbfit.util.*;
import fit.Binding;
import fit.Fixture;
import fit.Parse;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static dbfit.util.Direction.*;

/**
 * this class handles all cases where a statement should be executed for each row with
 * given inputs and verifying optional outputs or exceptions. it also handles a special case
 * when just a single statement is executed without binding parameters to columns. Examples are
 * - Inserting data into tables/views
 * - Executing statements
 * - Updates
 * - Stored procedures/functions
 * <p/>
 * the object under test is defined by overriding getTargetObject. Unfortunately, because of the way FIT
 * instantiates fixtures, passing in an object using a constructor and aggregation simply doesn't do the trick
 * so users have to extend this fixture.
 */
public abstract class DbObjectExecutionFixture extends Fixture {
    /**
     * override this method and supply the dbObject implementation that will be executed for each row
     */
    protected abstract DbObject getTargetDbObject() throws SQLException;

    /**
     * executes the target dbObject for all rows of the table. if no rows are specified, executes
     * the target object only once
     */
    public void doRows(Parse rows) {
        try {
            DbObject dbObject = getTargetDbObject();
            if (rows == null) {//single execution, no args
                StatementExecution preparedStatement = dbObject.buildPreparedStatement(DbParameterAccessors.EMPTY);
                preparedStatement.run();
            } else {
                List<String> columnNames = FitHelpers.getCellTextFrom(rows.parts);
                DbParameterAccessors accessors = new HeaderRow(columnNames, dbObject).getAccessors();
                StatementExecution execution = dbObject.buildPreparedStatement(accessors.toArray());
                Parse row = rows;
                while ((row = row.more) != null) {
                    newRowTest(accessors, execution).runRow(row);
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            if (rows == null) throw new Error(e);
            exception(rows.parts, e);
        }
    }

    protected RowTest newRowTest(DbParameterAccessors accessors, StatementExecution execution) {
        return new RowTest(accessors, execution, this);
    }

    public static class HeaderRow {
        private List<String> columnNames;
        private DbObject dbObject;

        public HeaderRow(List<String> columnNames, DbObject dbObject) {
            this.columnNames = columnNames;
            this.dbObject = dbObject;
        }

        public DbParameterAccessors getAccessors() throws SQLException {
            DbParameterAccessors accessors = new DbParameterAccessors();
            for (String name : columnNames) {
                DbParameterAccessor accessor = dbObject.getDbParameterAccessor(name, isOutput(name) ? OUTPUT : INPUT);
                if (accessor == null) throw new IllegalArgumentException("Parameter/column " + name + " not found");
                accessors.add(accessor);
            }
            return accessors;
        }

        private static boolean isOutput(String name) {
            return name.endsWith("?");
        }
    }

    public static class FitHelpers {
        public static List<String> getCellTextFrom(Parse cells) {
            List<String> cellText = new ArrayList<String>();
            for (; cells != null; cells = cells.more) {
                cellText.add(cells.text());
            }
            return cellText;
        }

        public static List<Parse> asCellList(Parse row) {
            List<Parse> cells = new ArrayList<Parse>();
            for (Parse cell = row.parts; cell != null; cell = cell.more) {
                cells.add(cell);
            }
            return cells;
        }
    }

    public static class RowTest {
        private DbParameterAccessors accessors;
        protected StatementExecution execution;
        protected Fixture parentFixture;
        private Map<DbParameterAccessor, Binding> columnBindings;

        public RowTest(DbParameterAccessors accessors, StatementExecution execution, Fixture parentFixture) {
            this.accessors = accessors;
            this.execution = execution;
            this.parentFixture = parentFixture;
        }

        /**
         * execute a single row
         */
        public void runRow(Parse row) throws Throwable {
            setInputs(row);
            run();
            evaluateOutputs(row);
        }

        private void run() {
            execution.run();
        }

        protected void setInputs(Parse row) throws Throwable {
            //first set input params
            Map<DbParameterAccessor, Parse> cellMap = accessors.zipWith(FitHelpers.asCellList(row));
            for (DbParameterAccessor inputAccessor : accessors.getInputAccessors()) {
                Parse cell = cellMap.get(inputAccessor);
                getColumnBindings().get(inputAccessor).doCell(parentFixture, cell);
            }
        }

        protected void evaluateOutputs(Parse row) throws Throwable {
            Map<DbParameterAccessor, Parse> cellMap = accessors.zipWith(FitHelpers.asCellList(row));
            for (DbParameterAccessor outputAccessor : accessors.getOutputAccessors()) {
                Parse cell = cellMap.get(outputAccessor);
                getColumnBindings().get(outputAccessor).doCell(parentFixture, cell);
            }
        }

        private Map<DbParameterAccessor, Binding> getColumnBindings() throws Exception {
            if (columnBindings == null) columnBindings = buildColumnBindings();
            return columnBindings;
        }

        /**
         * bind db accessors to columns based on the text in the header
         */
        private Map<DbParameterAccessor, Binding> buildColumnBindings() throws Exception {
            Map<DbParameterAccessor, Binding> bindings = new HashMap<DbParameterAccessor, Binding>();
            for (DbParameterAccessor accessor : accessors.toArray()) {
                Binding binding = (accessor.hasDirection(INPUT) ? new SymbolAccessSetBinding() : new SymbolAccessQueryBinding());
                binding.adapter = new DbParameterAccessorTypeAdapter(accessor, this.parentFixture);
                bindings.put(accessor, binding);
            }
            return bindings;
        }
    }
}
