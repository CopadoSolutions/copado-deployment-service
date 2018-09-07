package copado.job;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;


@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class OnPremiseDeploymentJobTest {

    @Autowired
    private OnPremiseDeploymentJob job;

    @Test
    public void test_doJob(){
        job.doJob("a0C0Y00000XTLeAUAX","promote_branch","target_branch","deployment_branch","DMD_Test~master~I294fb4a0a5cea1cb55026d21e6045140b230acfa");
    }

}