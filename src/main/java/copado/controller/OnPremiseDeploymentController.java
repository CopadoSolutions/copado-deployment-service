package copado.controller;

import copado.job.OnPremiseDeploymentJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/copado/v1/on-premise-deployment/")
public class OnPremiseDeploymentController {

    @Autowired
    private OnPremiseDeploymentJob job;

    @GetMapping("deploy")
    public ResponseEntity<String> onDeploy(){
        job.doJob("promote_branch","target_branch","deployment_branch","DMD_Test~master~I294fb4a0a5cea1cb55026d21e6045140b230acfa");
        return new ResponseEntity<String>("Deployed!", HttpStatus.OK);
    }
}
