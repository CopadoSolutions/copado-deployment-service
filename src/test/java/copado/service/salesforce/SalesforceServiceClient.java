package copado.service.salesforce;

import java.nio.file.Path;
import java.nio.file.Paths;

public class SalesforceServiceClient {


    // @Test
    //public void test_Deploy() throws Exception {

    public static void main(String[] args) throws Exception {

        ClassLoader classLoader = SalesforceServiceClient.class.getClassLoader();
        Path baseDir = Paths.get(classLoader.getResource("deploy_OK_2.zip").getPath());

        SalesforceService service = new SalesforceService();
        service.init();
        service.deployZip(baseDir.toAbsolutePath().toString());

    }


}