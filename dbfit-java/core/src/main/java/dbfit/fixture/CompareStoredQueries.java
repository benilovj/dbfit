package dbfit.fixture;

import dbfit.api.DBEnvironment;
import dbfit.diff.DataTableDiff;
import dbfit.fixture.report.ReportingSystem;
import dbfit.fixture.report.FitFixtureReportingSystem;
import dbfit.util.*;
import static dbfit.util.DataCell.createDataCell;
import static dbfit.util.MatchStatus.*;

import fit.Fixture;
import fit.Parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompareStoredQueries extends fit.Fixture {
    private String symbol1;
    private String symbol2;
    private DataTable dt1;
    private DataTable dt2;

    public CompareStoredQueries() {
    }

    public CompareStoredQueries(DBEnvironment environment, String symbol1, String symbol2) {
        this.symbol1 = symbol1;
        this.symbol2 = symbol2;
    }

    private void initialiseDataTables() {
        if (symbol1 == null || symbol2 == null) {
            if (args.length < 2)
                throw new UnsupportedOperationException("No symbols specified to CompareStoreQueries constructor or argument list");
            symbol1 = args[0];
            symbol2 = args[1];
        }
        dt1 = SymbolUtil.getDataTable(symbol1);
        dt2 = SymbolUtil.getDataTable(symbol2);
    }

    public void doTable(Parse table) {
        initialiseDataTables();
        Parse lastRow = table.parts.more;
        if (lastRow == null) {
            throw new Error("Query structure missing from second row");
        }

        DataTableDiff diff = new DataTableDiff(
                loadRowStructure(lastRow), getReporter(table));

        diff.diff(dt1, dt2);
    }

    private RowStructure loadRowStructure(Parse headerRow) {
        String[] columnNames;
        boolean[] keyProperties;

        Parse headerCell = headerRow.parts;
        int colNum = headerRow.parts.size();
        columnNames = new String[colNum];
        keyProperties = new boolean[colNum];
        for (int i = 0; i < colNum; i++) {
            String currentName = headerCell.text();
            if (currentName == null) throw new UnsupportedOperationException("Column " + i + " does not have a name");
            currentName = currentName.trim();
            if (currentName.length() == 0)
                throw new UnsupportedOperationException("Column " + i + " does not have a name");
            columnNames[i] = NameNormaliser.normaliseName(currentName);
            keyProperties[i] = !currentName.endsWith("?");
            headerCell = headerCell.more;
        }

        return new RowStructure(columnNames, keyProperties);
    }

    protected FitFixtureReporter getReporter(final Parse table) {
        return new FitFixtureReporter(
                new FitFixtureReportingSystem(this, table));
    }

    public static class FitFixtureReporter extends NoOpDiffListenerAdapter {
        protected ReportingSystem reportingSystem;

        public FitFixtureReporter(final ReportingSystem reportingSystem) {
            this.reportingSystem = reportingSystem;
        }

        public ReportingSystem getReportingSystem() {
            return reportingSystem;
        }

        @Override
        public void endRow(MatchResult<DataRow, DataRow> result) {
            reportingSystem.endRow(result);
        }

        @Override
        public void endCell(MatchResult<DataCell, DataCell> result) {
            reportingSystem.addCell(result);
        }
    }
}

