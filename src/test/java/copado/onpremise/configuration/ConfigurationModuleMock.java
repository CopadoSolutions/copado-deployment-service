package copado.onpremise.configuration;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import org.apache.commons.configuration.CompositeConfiguration;

public class ConfigurationModuleMock extends AbstractModule {

    private static ConfigurationModule realConfigurationModule;

    private static ConfigurationModuleDataSet dataSet;

    public static void setUpConfig(String remoteMainRepositoryPath, String firstArtifactRemotePath, String secondArtifactRemotePath) {
        final String deploymentOrgId = "TEST_DEPLOYMENT_ORG_ID";
        final String firstArtifactRepositoryId = "ARTIFACT_REPOSITORY_ID_1";
        final String secondArtifactRepositoryId = "ARTIFACT_REPOSITORY_ID_2";
        final String correctAuthor = "TEST_AUTHOR";
        final String correctAuthorEmail = "TEST_AUTHOR@email.com";

        CompositeConfiguration compositeConfiguration = new CompositeConfiguration();
        // Salesforce: Copado org
        compositeConfiguration.setProperty("copado.onpremise.deployment.copadoUrl", "https://login.salesforce.com/services/Soap/u/44.0");
        compositeConfiguration.setProperty("copado.onpremise.deployment.copadoUsername", "TEST_COPADO_USENAME");
        compositeConfiguration.setProperty("copado.onpremise.deployment.copadoPassword", "TEST_COPADO_PASSWORD");
        compositeConfiguration.setProperty("copado.onpremise.deployment.copadoToken", "TEST_COPADO_TOKEN");

        // Salesforce: Deployment org
        compositeConfiguration.setProperty("copado.onpremise.deployment." + deploymentOrgId + ".url", "https://login.salesforce.com/services/Soap/u/44.0");
        compositeConfiguration.setProperty("copado.onpremise.deployment." + deploymentOrgId + ".username", "TEST_DEPLOYMENT_USENAME");
        compositeConfiguration.setProperty("copado.onpremise.deployment." + deploymentOrgId + ".token", "TEST_DEPLOYMENT_PASSWORD");
        compositeConfiguration.setProperty("copado.onpremise.deployment." + deploymentOrgId + ".password", "TEST_DEPLOYMENT_TOKEN");

        // Git: Main repository
        compositeConfiguration.setProperty("copado.onpremise.deployment.gitUrl", remoteMainRepositoryPath);
        compositeConfiguration.setProperty("copado.onpremise.deployment.gitUsername", correctAuthor);
        compositeConfiguration.setProperty("copado.onpremise.deployment.gitPassword", correctAuthorEmail);

        // Git: Artifact repository
        compositeConfiguration.setProperty("copado.onpremise.deployment." + firstArtifactRepositoryId + ".url", firstArtifactRemotePath);
        compositeConfiguration.setProperty("copado.onpremise.deployment." + firstArtifactRepositoryId + ".username", correctAuthor);
        compositeConfiguration.setProperty("copado.onpremise.deployment." + firstArtifactRepositoryId + ".password", correctAuthorEmail);

        compositeConfiguration.setProperty("copado.onpremise.deployment." + secondArtifactRepositoryId + ".url", secondArtifactRemotePath);
        compositeConfiguration.setProperty("copado.onpremise.deployment." + secondArtifactRepositoryId + ".username", correctAuthor);
        compositeConfiguration.setProperty("copado.onpremise.deployment." + secondArtifactRepositoryId + ".password", correctAuthorEmail);

        realConfigurationModule = new ConfigurationModule(compositeConfiguration);

        dataSet = ConfigurationModuleDataSet.builder()
                .compositeConfiguration(compositeConfiguration)
                .applicationConfiguration(realConfigurationModule.applicationConfiguration())
                .build();



    }

    @Provides
    CompositeConfiguration compositeConfiguration() {
        return dataSet.getCompositeConfiguration();
    }

    @Provides
    ApplicationConfiguration applicationConfiguration() {
        return dataSet.getApplicationConfiguration();
    }


}