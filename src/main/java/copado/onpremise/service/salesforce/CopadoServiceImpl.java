package copado.onpremise.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.exception.CopadoException;
import lombok.extern.flogger.Flogger;

import javax.inject.Inject;


@Flogger
public class CopadoServiceImpl implements CopadoService {

    private PartnerConnection connection;

    private ApplicationConfiguration conf;

    private PartnerConnectionBuilder partnerConnectionBuilder;


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

    /**
     * ID por param
     * <p>
     * Deployer.java
     */
    public void updateDeploymentJobStatus(String id, String status) {

        log.atInfo().log("Updating Deployment Job[%s], Status:'%s' ", id, status);

        if (id == null) {
            log.atSevere().log("Could not update job status because id is null");
            return;
        }

        SObject object = new SObject();
        object.setType(getNamespace() + "Deployment_Job__c");
        object.setField(getNamespace() + "Status__c", status);
        object.setId(id);

        try {
            SaveResult[] resultArr = connection.update(new SObject[]{object});
            if (resultArr == null || resultArr.length <= 0) {
                throw new CopadoException("Not result found for status update");
            }
            log.atInfo().log("Could update status:'%s', result:'%s'", resultArr[0].getSuccess(), resultArr);
        } catch (CopadoException e) {
            log.atSevere().withCause(e).log("Error updating status");
        } catch (Exception e) {
            log.atSevere().withCause(e).log("Error updating status:'%s'");
        }
    }


    private String getNamespace() {
        String ns = conf.getRenameNamespace();
        return ns != null ? ns : "copado__";
    }

}
