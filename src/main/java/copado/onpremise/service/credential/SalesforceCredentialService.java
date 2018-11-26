package copado.onpremise.service.credential;

/**
 * Service that allows to retrieve the credentials for your organizations.
 */
public interface SalesforceCredentialService {

    /**
     * Given a salesforce organization identifier, this function will return the related credentials in order to do, later, a manual login
     * through the metadata API.
     * @param orgId
     * @return
     */
    SalesforceCredentials getCredentials(String orgId);

}
