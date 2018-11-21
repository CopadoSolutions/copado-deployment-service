package copado.controller;

import copado.job.OnPremiseDeploymentJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/copado/v1/on-premise-deployment/")
public class OnPremiseDeploymentController {

    @Autowired
    private OnPremiseDeploymentJob onPremiseDeploymentJob;

    @PostMapping("deploy")
    public ResponseEntity<String> onDeploy(@RequestBody DeployRequest request) {
        onPremiseDeploymentJob.doJob(request);
        return new ResponseEntity<>("Deploying...!", HttpStatus.OK);
    }
}
