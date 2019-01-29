package copado.onpremise.service.credential;


import com.google.inject.Inject;
import copado.onpremise.configuration.ApplicationConfiguration;
import lombok.AllArgsConstructor;

@AllArgsConstructor(onConstructor = @__({@Inject}))
class GitCredentialsServiceImpl implements GitCredentialService {

    private static final String PREFIX = "copado.onpremise.deployment";

    private PropertyCredentialsProvider propertyCredentialsProviderProvider;

    private ApplicationConfiguration applicationConfiguration;

    public GitCredentials getCredentials(String gitRepositoryId) {

        return GitCredentialsImpl.builder()
                .url(propertyCredentialsProviderProvider.getCredentialField(gitRepositoryId, PREFIX, "url"))
                .username(propertyCredentialsProviderProvider.getCredentialField(gitRepositoryId, PREFIX, "username"))
                .password(propertyCredentialsProviderProvider.getCredentialField(gitRepositoryId, PREFIX, "password"))
                .build();
    }

    public GitCredentials getCredentialsForMainRepository() {

        return GitCredentialsImpl.builder()
                .url(applicationConfiguration.getGitUrl())
                .username(applicationConfiguration.getGitUsername())
                .password(applicationConfiguration.getGitPassword())
                .build();
    }

}
