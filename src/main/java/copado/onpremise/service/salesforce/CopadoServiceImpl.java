package copado.onpremise.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.exception.CopadoException;
import lombok.extern.flogger.Flogger;

import javax.inject.Inject;
import java.util.List;


@Flogger
public class CopadoServiceImpl implements CopadoService {

    private PartnerConnection connection;

    private ApplicationConfiguration conf;

    private PartnerConnectionBuilder partnerConnectionBuilder;

    private SalesforceService salesforceService;

    @Inject
    public CopadoServiceImpl(ApplicationConfiguration conf, PartnerConnectionBuilder partnerConnectionBuilder) throws ConnectionException {
        this.conf = conf;
        this.partnerConnectionBuilder = partnerConnectionBuilder;
        init();
    }

    private void init() throws ConnectionException {

        connection = partnerConnectionBuilder.createPartnerConnection(
                SalesforceUtilsInfo.builder()
                        .username(conf.getCopadoUsername())
                        .password(conf.getCopadoPassword())
                        .token(conf.getCopadoToken())
                        .loginUrl(conf.getCopadoUrl())
                        .proxyHost(conf.getProxyHost())
                        .proxyPort(conf.getProxyPort())
                        .proxyUsername(conf.getProxyUsername())
                        .proxyPassword(conf.getProxyPassword())
                        .build()
        );
    }



    @Override
    public void updateDeploymentJobStatus(String id, String status) {
        try {
            salesforceService.updateStringField(connection, id, getNamespace() + "Deployment_Job__c", getNamespace() + "Status__c", status);
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Could not update deployment status");
        }
    }

    @Override
    public void updateDeploymentJobValidationId(String id, String validationId) throws CopadoException {
        salesforceService.updateStringField(connection, id, getNamespace() + "Deployment_Job__c", getNamespace() + "Validation_ID__c", validationId);
    }

    @Override
    public void updateDeploymentJobAsyncId(String id, String asyncId) throws CopadoException {
        salesforceService.updateStringField(connection, id, getNamespace() + "Deployment_Job__c", getNamespace() + "Async_Job_ID__c", asyncId);
    }

    @Override
    public String getSourceOrgId(String deploymentJobId) throws CopadoException {

        log.atInfo().log("Reading source org id for deployment job[%s]", deploymentJobId);
        if (deploymentJobId == null) {
            log.atSevere().log("Invalid deployment job id");
            throw new CopadoException("Invalid deployment job id");
        }

        List<SObject> result;
        try {
            result = salesforceService.query(connection, buildGetSourceOrgIdQuery(deploymentJobId));
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Search for source org identifier failed");
            throw new CopadoException("Search for source org identifier failed");
        }

        if (result.size() != 1) {
            log.atSevere().log("Unexpected result size for source org identifier: %s", result.size());
            throw new CopadoException("Unexpected result size for source org identifier: " + result.size());
        }

        String sourceOrgId = (String) result.get(0)
                .getChild("copado__Step__r")
                .getChild("copado__Deployment__r")
                .getChild("copado__From_Org__r")
                .getChild("copado__Environment__r")
                .getField("copado__Org_ID__c");

        log.atInfo().log("Retrieved source org id: %s", sourceOrgId);
        return sourceOrgId;
    }

    public String getDeploymentId(String deploymentJobId) throws CopadoException {
        //TODO:: Sigo aqui

        log.atInfo().log("Reading deployment identifier for deployment job[%s]", deploymentJobId);
        if (deploymentJobId == null) {
            log.atSevere().log("Invalid deployment job id");
            throw new CopadoException("Invalid deployment job id");
        }

        String query = String.format("SELECT copado__Step__r.copado__Deployment__r.Id FROM copado__Deployment_Job__c WHERE Id = '%s'", deploymentJobId);
        List<SObject> result;
        try {
            result = salesforceService.query(connection, query);
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
                .getChild("copado__Step__r")
                .getChild("copado__Deployment__r")
                .getField("Id");

        log.atInfo().log("Retrieved deployment identifier: %s", sourceOrgId);
        return sourceOrgId;

    }

    @Override
    public String createTxtAttachment(String parentId, String attachmentName, String attachmentContent) throws CopadoException {
        return salesforceService.createTxtAttachment(connection, parentId, attachmentName, attachmentContent);
    }

    private String buildGetSourceOrgIdQuery(String deploymentJobId) {
        return String.format("SELECT copado__Step__r.copado__Deployment__r.copado__From_Org__r.copado__Environment__r.copado__Org_ID__c FROM copado__Deployment_Job__c WHERE Id = '%s'", deploymentJobId);
    }


    private String getNamespace() {
        String ns = conf.getRenameNamespace();
        return ns != null ? ns : "copado__";
    }

}
