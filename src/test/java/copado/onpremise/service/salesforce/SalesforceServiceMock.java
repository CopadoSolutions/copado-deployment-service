package copado.onpremise.service.salesforce;

import com.google.common.io.Files;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public class SalesforceServiceMock implements SalesforceService {

    private static SalesforceDataSet dataSet;
    private static SalesforceDataSource dataSource;

    public static void setUp(SalesforceDataSource dataSource, SalesforceDataSet dataSet) {
        SalesforceServiceMock.dataSource = dataSource;
        SalesforceServiceMock.dataSet = dataSet;
    }

    public static void setUp(SalesforceDataSource dataSource) {

        SalesforceServiceMock.dataSet = SalesforceDataSet.builder()
                .deploymentResult(
                        DeploymentResult.builder()
                        .asyncId("TEST_ASYNC_ID")
                        .tips(emptyList())
                        .success(true)
                        .deployedZipPath(mock(Path.class))
                        .build())
                .attachmentId("TEST_ATTACHMENT_ID")
                .build();
    }

    @Override
    public DeploymentResult deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath, DeployRequest deployRequest, SalesforceDeployerDelegate delegate) throws CopadoException {
        try {
            assertThat(Files.toByteArray(Paths.get(zipFileAbsolutePath).toFile()), is(equalTo(dataSource.getCorrectZipBytes())));
        } catch (IOException e) {
            throw new RuntimeException("Could not read deploy zip bytes", e);
        }

        return dataSet.getDeploymentResult();
    }

    @Override
    public List<SObject> query(PartnerConnection partnerConnection, String query) throws CopadoException {
        return emptyList();
    }

    @Override
    public void updateStringField(PartnerConnection partnerConnection, String id, String objectType, String fieldName, String value) throws CopadoException {

    }

    @Override
    public String createTxtAttachment(PartnerConnection partnerConnection, String parentId, String attachmentName, String attachmentContent) throws CopadoException {
        return dataSet.getAttachmentId();
    }
}
