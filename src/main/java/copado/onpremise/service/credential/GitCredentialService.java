package copado.onpremise.service.credential;

/**
 * Service that allows to retrieve the credentials for your repositories.
 */
public interface GitCredentialService {

    /**
     * Given a salesforce copado-git-repository identifier, this function will return the related credentials.
     *
     * @param orgId
     * @return
     */
    GitCredentials getCredentials(String orgId);

    /**
     * Provides the credentials to acccess into the main repository, where the deployment branch with the payload is stored.
     * @return
     */
    GitCredentials getCredentialsForMainRepository();


}
