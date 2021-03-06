package copado.onpremise.connector.copado;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.connector.salesforce.PartnerConnectionBuilder;
import copado.onpremise.connector.salesforce.data.SalesforceService;
import copado.onpremise.connector.salesforce.data.SalesforceUtilsInfo;
import copado.onpremise.exception.CopadoException;
import lombok.extern.flogger.Flogger;

import javax.inject.Inject;
import java.util.List;

@Flogger
public class CopadoServiceImpl implements CopadoService {

    private static final String DEPLOYMENT_JOB_C = "Deployment_Job__c";
    private static final String ERROR_MESSAGE_INVALID_DEPLOYMENT_JOB_ID = "Invalid deployment job id";

    private PartnerConnection partnerConnection;

    private ApplicationConfiguration appConf;

    private PartnerConnectionBuilder partnerConnectionBuilder;

    private SalesforceService salesforceService;

    @Inject
    public CopadoServiceImpl(ApplicationConfiguration appConf, PartnerConnectionBuilder partnerConnectionBuilder, SalesforceService salesforceService) throws CopadoException {
        this.appConf = appConf;
        this.partnerConnectionBuilder = partnerConnectionBuilder;
        this.salesforceService = salesforceService;
        init();
    }

    private void init() throws CopadoException {

        partnerConnection = partnerConnectionBuilder.createPartnerConnection(
                SalesforceUtilsInfo.builder()
                        .username(appConf.getCopadoUsername())
                        .password(appConf.getCopadoPassword())
                        .token(appConf.getCopadoToken())
                        .loginUrl(appConf.getCopadoUrl())
                        .proxyHost(appConf.getProxyHost())
                        .proxyPort(appConf.getProxyPort())
                        .proxyUsername(appConf.getProxyUsername())
                        .proxyPassword(appConf.getProxyPassword())
                        .build()
        );
    }


    @Override
    public void updateDeploymentJobStatus(String id, String status) {
        try {
            salesforceService.updateStringField(partnerConnection, id, getNamespace() + DEPLOYMENT_JOB_C, getNamespace() + "Status__c", status);
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Could not update deployment status");
        }
    }

    @Override
    public void updateDeploymentJobValidationId(String id, String validationId) throws CopadoException {
        salesforceService.updateStringField(partnerConnection, id, getNamespace() + DEPLOYMENT_JOB_C, getNamespace() + "Validation_ID__c", validationId);
    }

    @Override
    public void updateDeploymentJobAsyncId(String id, String asyncId) throws CopadoException {
        salesforceService.updateStringField(partnerConnection, id, getNamespace() + DEPLOYMENT_JOB_C, getNamespace() + "Async_Job_ID__c", asyncId);
    }

    @Override
    public String getSourceOrgId(String deploymentJobId) throws CopadoException {

        log.atInfo().log("Reading source org id for deployment job[%s]", deploymentJobId);
        if (deploymentJobId == null) {

            log.atSevere().log(ERROR_MESSAGE_INVALID_DEPLOYMENT_JOB_ID);
            throw new CopadoException(ERROR_MESSAGE_INVALID_DEPLOYMENT_JOB_ID);
        }

        List<SObject> result;
        try {
            result = salesforceService.query(partnerConnection, buildGetSourceOrgIdQuery(deploymentJobId));
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Search for source org identifier failed");
            throw new CopadoException("Search for source org identifier failed");
        }

        if (result.size() != 1) {
            log.atSevere().log("Unexpected result size for source org identifier: %s", result.size());
            throw new CopadoException("Unexpected result size for source org identifier: " + result.size());
        }

        String sourceOrgId = (String) result.get(0)
                .getChild(replaceCopadoNamespace("copado__Step__r"))
                .getChild(replaceCopadoNamespace("copado__Deployment__r"))
                .getChild(replaceCopadoNamespace("copado__From_Org__r"))
                .getChild(replaceCopadoNamespace("copado__Environment__r"))
                .getField(replaceCopadoNamespace("copado__Org_ID__c"));

        log.atInfo().log("Retrieved source org id: %s", sourceOrgId);
        return sourceOrgId;
    }

    public String getDeploymentId(String deploymentJobId) throws CopadoException {

        log.atInfo().log("Reading deployment identifier for deployment job[%s]", deploymentJobId);
        if (deploymentJobId == null) {
            log.atSevere().log(ERROR_MESSAGE_INVALID_DEPLOYMENT_JOB_ID);
            throw new CopadoException(ERROR_MESSAGE_INVALID_DEPLOYMENT_JOB_ID);
        }

        String query = String.format(replaceCopadoNamespace("SELECT copado__Step__r.copado__Deployment__r.Id FROM copado__Deployment_Job__c WHERE Id = '%s'"), deploymentJobId);
        List<SObject> result;
        try {
            result = salesforceService.query(partnerConnection, query);
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Search for deployment identifier failed");
            throw new CopadoException("Search for deployment identifier failed");
        }

        if (result.size() != 1) {
            String errorMessage = String.format("Unexpected result size for deployment identifier: %s", result.size());
            log.atSevere().log(errorMessage);
            throw new CopadoException(errorMessage);
        }

        String sourceOrgId = (String) result.get(0)
                .getChild(replaceCopadoNamespace("copado__Step__r"))
                .getChild(replaceCopadoNamespace("copado__Deployment__r"))
                .getField("Id");

        log.atInfo().log("Retrieved deployment identifier: %s", sourceOrgId);
        return sourceOrgId;

    }

    @Override
    public String createTxtAttachment(String parentId, String attachmentName, String attachmentContent) throws CopadoException {
        return salesforceService.createTxtAttachment(partnerConnection, parentId, attachmentName, attachmentContent);
    }

    private String buildGetSourceOrgIdQuery(String deploymentJobId) {
        return String.format(replaceCopadoNamespace("SELECT copado__Step__r.copado__Deployment__r.copado__From_Org__r.copado__Environment__r.copado__Org_ID__c FROM copado__Deployment_Job__c WHERE Id = '%s'"), deploymentJobId);
    }

    private String replaceCopadoNamespace(String originalStr) {
        return originalStr.replace("copado__", getNamespace());
    }

    private String getNamespace() {
        String ns = appConf.getRenameNamespace();
        return ns != null ? ns : "copado__";
    }

}
