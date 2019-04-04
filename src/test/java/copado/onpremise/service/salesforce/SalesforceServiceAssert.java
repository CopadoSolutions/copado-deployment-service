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
import static org.mockito.Mockito.mock;

@Flogger
public class SalesforceServiceAssert implements SalesforceService {

    private static SalesforceDataSet dataSet;
    private static SalesforceServiceLog serviceLog;


    public static void setUpSalesforce() {
        SalesforceServiceAssert.serviceLog = new SalesforceServiceLog();
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

    public static SalesforceServiceLog salesforceServiceLog() {
        return serviceLog;
    }

    @Override
    public DeploymentResult deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath, DeployRequest deployRequest, SalesforceDeployerDelegate delegate) throws CopadoException {
        serviceLog.getDeployRequests().add(deployRequest);
        serviceLog.getZipsBytes().add(bytesOf(zipFileAbsolutePath));
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
