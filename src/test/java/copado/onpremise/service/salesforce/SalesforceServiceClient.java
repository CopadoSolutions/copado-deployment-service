package copado.onpremise.service.salesforce;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SalesforceServiceClient {

    @Autowired
    private MetadataConnectionService metadataConnectionService;

    @Test
    public void test_Deploy() throws Exception {

        ClassLoader classLoader = SalesforceServiceClient.class.getClassLoader();
        Path baseDir = Paths.get(classLoader.getResource("deploy_OK_2.zip").getPath());

        SalesforceService service = new SalesforceService();
        service.deployZip(metadataConnectionService.build("ORG_ID"), baseDir.toAbsolutePath().toString());

    }

}