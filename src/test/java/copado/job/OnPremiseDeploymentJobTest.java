package copado.job;

import copado.service.gerrit.GerritService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;

import static org.mockito.ArgumentMatchers.anyString;


@RunWith(SpringRunner.class)
@SpringBootTest
@Import(SpringTestConfig.class)
public class OnPremiseDeploymentJobTest {

   // @MockBean
   // private CopadoService copadoService;

    @MockBean
    private GerritService gerritService;

    @Autowired
    private OnPremiseDeploymentJob job;


    @Test
    public void test_doJob(){

       //Mockito.doNothing().when(copadoService).updateDeploymentJobStatus(anyString(),anyString());

        Mockito.doReturn(true).when(gerritService).isValidChange(anyString());

        job.doJob("ID","promote_branch","target_branch","deployment_branch","DMD_Test~master~I294fb4a0a5cea1cb55026d21e6045140b230acfa");
    }

}