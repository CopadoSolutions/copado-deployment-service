package copado.onpremise.job;

import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class JobRunner {

    @Autowired
    private OnPremiseDeploymentJob onPremiseDeploymentJob;

    @Autowired
    private Path payloadDirPath;

    @PostConstruct
    public void init() throws IOException {

        DeployRequest request = new ObjectMapper().readValue(payloadDirPath.resolve("payload.json").toFile(), DeployRequest.class);
        onPremiseDeploymentJob.doJob(request);

    }
}
