package copado.controller;

import copado.job.OnPremiseDeploymentJob;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/copado/v1/on-premise-deployment/")
public class OnPremiseDeploymentController {

    @Autowired
    private OnPremiseDeploymentJob job;

    @GetMapping("deploy")
    public ResponseEntity<String> onDeploy(@RequestParam("promoteBranch") String promoteBranch,
                                           @RequestParam("targetBranch") String targetBranch,
                                           @RequestParam("deploymentBranch") String deploymentBranch,
                                           @RequestParam("gerritChangeId") String gerritChangeId
    ){
        job.doJob(promoteBranch,targetBranch,deploymentBranch,gerritChangeId);
        return new ResponseEntity<String>("Deploying...!", HttpStatus.OK);
    }
}
