package copado.onpremise.connector.git;

import copado.onpremise.service.credential.GitCredentials;

import java.nio.file.Path;


public interface GitSession {

    Path getBaseDir();
    GitCredentials getGitCredentials();

}
