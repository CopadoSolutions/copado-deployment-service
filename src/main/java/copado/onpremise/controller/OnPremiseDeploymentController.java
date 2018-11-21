package copado.onpremise.controller;

import copado.onpremise.job.OnPremiseDeploymentJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/copado/onpremise/v1/deployment/")
public class OnPremiseDeploymentController {

    @Autowired
    private OnPremiseDeploymentJob onPremiseDeploymentJob;

    @PostMapping("deploy")
    public ResponseEntity<String> onDeploy(@RequestBody DeployRequest request) {
        onPremiseDeploymentJob.doJob(request);
        return new ResponseEntity<>("Deploying...!", HttpStatus.OK);
    }
}
