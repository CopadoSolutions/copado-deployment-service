package copado.service.salesforce;

import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.partner.*;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.bind.XmlObject;
import com.sun.deploy.ref.Helpers;
import copado.util.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class CopadoService {

    private PartnerConnection connection;

    @PostConstruct
    public void init() throws ConnectionException {

        connection = SalesforceUtils.createPartnerConnection(
                SystemProperties.COPADO_USERNAME.value(),
                SystemProperties.COPADO_PASSWORD.value(),
                SystemProperties.COPADO_TOKEN.value(),
                SystemProperties.COPADO_URL.value()
        );
    }
/*
    public String createDeploymentRecord(SObject promotion, String promotionId, boolean sendEmail, Optional<String> deploymentName) throws ConnectionException {

        GetServerTimestampResult tsr = connection.getServerTimestamp();
        SObject deployment = newDeployment(tsr.getTimestamp());
        deployment.setField( "copado__Promotion__c", promotionId);
        deployment.setField( "copado__Send_Deployment_Complete_email__c", sendEmail);
        deployment.setField( "copado__From_Org__c",deployment.getField("copado__Source_Org_Credential__c"));
        String tz = connection.getUserInfo().getUserTimeZone();


        String deploymentNameStr = getDeploymentName(promotion,deploymentName,tz,tsr);
        deployment.setField( "Name", StringUtils.left(deploymentNameStr, 80));
        connection.create(new SObject[] {deployment});

        return deployment.getId();
    }

    public static SObject newDeployment(Calendar d) {
        SObject deployment = new SObject();
        deployment.setType("copado__Deployment__c");
        deployment.setField("copado__Status__c", "Scheduled");
        deployment.setField( "copado__Date__c", d);
        deployment.setField("copado__Schedule__c", "Deploy now");
        return deployment;
    }

    private static String getDeploymentName(SObject promotion, Optional<String> deploymentName, String tz,    GetServerTimestampResult tsr ){
        String deploymentNameStr;
        if (!deploymentName.isPresent()) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            sdf.setTimeZone(TimeZone.getTimeZone(tz));
            String now = sdf.format(tsr.getTimestamp().getTime());
            deploymentNameStr = "Promotion:" + " - " + promotion.getField("Name").toString() + " " + now;
        }else{
            deploymentNameStr = deploymentName.get();
        }
        return  deploymentNameStr;
    }*/


    /**
     * ID por param
     *
     * Deployer.java
     */


    public void updateDeploymentJobStatus(String status, String id) {

        log.info("Updating Deployment Job status:'{}' id:'{}'", status, id);
        SObject object = new SObject();
        object.setType("copado__Deployment_Job__c");
        object.setField("copado__Status__c", status);
        object.setId(id);

        try {
            connection.update(new SObject[] { object });
        } catch (Exception e) {
            log.error("Error updating status with message:'{}'", e.getMessage(), e);
        }
    }

}
