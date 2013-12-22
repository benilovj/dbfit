package dbfit.util.actions;

import dbfit.util.Cell;
import dbfit.util.TestResultHandler;

public interface Action {
    public void run(Cell cell) throws Exception;
    public boolean appliesTo(Cell cell);
}
