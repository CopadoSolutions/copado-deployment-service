package copado.onpremise.service.salesforce;

import com.sforce.soap.metadata.TestLevel;

public class TestLevelBuilder {

    public static TestLevel build(String testLevel){
        switch (testLevel) {
            case "RunAllTestsInOrg":
                return TestLevel.RunAllTestsInOrg;
            case "RunLocalTests":
                return TestLevel.RunLocalTests;
            case "RunSpecifiedTests":
                return TestLevel.RunSpecifiedTests;
            case "NoTestRun":
                return TestLevel.NoTestRun;
            default:
                return TestLevel.NoTestRun;
        }
    }
}
