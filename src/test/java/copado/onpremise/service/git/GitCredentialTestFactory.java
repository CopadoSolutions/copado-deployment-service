package copado.onpremise.service.git;

import copado.onpremise.service.credential.GitCredentials;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class GitCredentialTestFactory {

    public static GitCredentials buildCorrectCredentials(String testFolder){
        final GitCredentials givenGitCredentials = mock(GitCredentials.class);
        when(givenGitCredentials.getPassword()).thenReturn("");
        when(givenGitCredentials.getUsername()).thenReturn("");
        when(givenGitCredentials.getUrl()).thenReturn(testFolder);
        return givenGitCredentials;
    }
}
