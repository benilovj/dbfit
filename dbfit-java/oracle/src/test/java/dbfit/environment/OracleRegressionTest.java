package dbfit.environment;

import fitnesse.junit.JUnitHelper;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

public class OracleRegressionTest {

    private JUnitHelper helper;

    @Before
    public void setUp() {
        helper = new JUnitHelper("../..", new File(System.getProperty("java.io.tmpdir"),"fitnesse").getAbsolutePath());
        helper.setPort(1234);
    }

    @Test
    public void flowMode() throws Exception {
        helper.assertSuitePasses("DbFit.AcceptanceTests.JavaTests.OracleTests.FlowMode");
    }

    @Test
    public void standaloneMode() throws Exception {
        helper.assertSuitePasses("DbFit.AcceptanceTests.JavaTests.OracleTests.StandaloneFixtures");
    }
}