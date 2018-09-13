package copado.controller;

import copado.job.OnPremiseDeploymentJob;
import copado.service.gerrit.GerritService;
import copado.service.salesforce.CopadoService;
import copado.service.salesforce.SalesforceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@SpringBootTest
@ActiveProfiles(profiles = "test")
public class OnPremiseDeploymentControllerTest {

    @Autowired
    private OnPremiseDeploymentController controller;

    @MockBean
    private OnPremiseDeploymentJob onPremiseDeploymentJob;

    @MockBean
    private GerritService gerritService;

    @MockBean
    private CopadoService copadoService;

    @MockBean
    private SalesforceService salesforceService;

    @Test
    public void onDeploy() {
        ResponseEntity<String> re = controller.onDeploy("djId", "pBranch", "tBranch", "dBranch", "gcId");
        assertEquals("Deploying...!", re.getBody());
        assertEquals(HttpStatus.OK, re.getStatusCode());
    }
}