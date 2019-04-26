package copado.onpremise.connector.salesforce;

import com.sforce.soap.metadata.TestLevel;
import org.junit.Test;

import static copado.onpremise.connector.salesforce.TestLevelBuilder.build;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;

public class TestLevelBuilderTest {


    @Test
    public void build_RunAllTestsInOrg(){
        assertThat(build("RunAllTestsInOrg"),is(equalTo(TestLevel.RunAllTestsInOrg)));
    }

    @Test
    public void build_RunLocalTests(){
        assertThat(build("RunLocalTests"),is(equalTo(TestLevel.RunLocalTests)));
    }

    @Test
    public void build_RunSpecifiedTests(){
        assertThat(build("RunSpecifiedTests"),is(equalTo(TestLevel.RunSpecifiedTests)));
    }

    @Test
    public void build_NoTestRun(){
        assertThat(build("NoTestRun"),is(equalTo(TestLevel.NoTestRun)));
    }

    @Test
    public void build_withNotSupported(){
        assertThat(build("NOT_SUPPORTED"),is(equalTo(TestLevel.NoTestRun)));
    }


}