package copado.service.salesforce;

import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.exception.CopadoException;
import copado.util.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class CopadoService {

    private PartnerConnection connection;

    @PostConstruct
    public void init() throws ConnectionException {

        connection = SalesforceUtils.createPartnerConnection(
                SalesforceUtilsInfo.builder()
                        .username(SystemProperties.ORGID_USERNAME.value())
                        .password(SystemProperties.ORGID_PASSWORD.value())
                        .token(SystemProperties.ORGID_TOKEN.value())
                        .loginUrl(SystemProperties.ORGID_URL.value())
                        .proxyHost(SystemProperties.PROXY_HOST.value())
                        .proxyPort(SystemProperties.PROXY_PORT.value())
                        .proxyUsername(SystemProperties.PROXY_USERNAME.value())
                        .proxyPassword(SystemProperties.PROXY_PASSWORD.value())
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


    private static String getNamespace() {
        String ns = SystemProperties.RENAME_NAMESPACE.value();
        return ns != null ? ns : "copado__";
    }

}
