package copado.onpremise.service.salesforce;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;
import lombok.extern.flogger.Flogger;

import java.nio.file.Path;
import java.util.List;

import static copado.onpremise.service.FileTestFactory.bytesOf;
import static java.util.Collections.emptyList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

@Flogger
public class SalesforceServiceAssert implements SalesforceService {

    private static SalesforceDataSet dataSet;
    private static SalesforceDataSource dataSource;

    public static void setUpSalesforce(SalesforceDataSource dataSource) {
        SalesforceServiceAssert.dataSource = dataSource;
        SalesforceServiceAssert.dataSet = SalesforceDataSet.builder()
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
            log.atInfo().log("Given path: %s", zipFileAbsolutePath);
            assertThat(bytesOf(zipFileAbsolutePath), is(equalTo(dataSource.getCorrectZipBytes())));
            assertThat(deployRequest,is(equalTo(dataSource.getDeployRequest())));
        } catch (Exception e) {

            String errorMessage = String.format("Could not read deployment zip bytes of zip: %s", zipFileAbsolutePath);
            log.atSevere().withCause(e).log(errorMessage);
            throw new RuntimeException(errorMessage, e);
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
