package copado.onpremise.controller;

import copado.onpremise.job.*;
import copado.onpremise.service.validation.ValidationService;
import copado.onpremise.service.salesforce.CopadoService;
import copado.onpremise.service.salesforce.SalesforceService;
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
    private ValidationService validationService;

    @MockBean
    private CopadoService copadoService;

    @MockBean
    private SalesforceService salesforceService;

    @Test
    public void onDeploy() {
        DeployRequest deployRequest = new DeployRequest();
        deployRequest.setCopadoJobId("copadoJobIdTest");
        deployRequest.setPromoteBranch("pBranch");
        deployRequest.setDeploymentBranch("dBranch");
        deployRequest.setTargetBranch("tBranch");
        deployRequest.setDeploymentJobId("djId");

        ResponseEntity<String> re = controller.onDeploy(deployRequest);
        assertEquals("Deploying...!", re.getBody());
        assertEquals(HttpStatus.OK, re.getStatusCode());
    }
}