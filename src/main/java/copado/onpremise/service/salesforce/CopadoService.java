package copado.onpremise.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.onpremise.ApplicationConfiguration;
import copado.onpremise.exception.CopadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class CopadoService {

    private PartnerConnection connection;

    @Autowired
    private ApplicationConfiguration conf;

    @PostConstruct
    public void init() throws ConnectionException {

        connection = SalesforceUtils.createPartnerConnection(
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

        log.info("Updating Deployment Job status:'{}' id:'{}'", status, id);
        SObject object = new SObject();
        object.setType(getNamespace() + "Deployment_Job__c");
        object.setField(getNamespace() + "Status__c", status);
        object.setId(id);

        try {
            SaveResult[] resultArr = connection.update(new SObject[]{object});
            if (resultArr == null || resultArr.length <= 0) {
                throw new CopadoException("Not result found for status update");
            }

            log.info("Could update status:'{}', result:'{}'", resultArr[0].getSuccess(), resultArr);

        } catch (CopadoException e) {
            log.error("Error updating status:'{}'", e.getMessage());
        } catch (Exception e) {
            log.error("Error updating status:'{}'", e);
        }
    }


    private String getNamespace() {
        String ns = conf.getRenameNamespace();
        return ns != null ? ns : "copado__";
    }

}
