package copado.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/copado/v1/on-premise-deployment/")
public class OnPremiseDeploymentController {

    @GetMapping("deploy")
    public ResponseEntity<String> onDeploy(){
        return new ResponseEntity<String>("Deployed!", HttpStatus.OK);
    }
}
