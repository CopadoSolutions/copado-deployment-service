package copado.onpremise;

import com.google.inject.Guice;
import com.google.inject.Injector;
import copado.onpremise.configuration.ConfigurationModule;
import copado.onpremise.job.JobModule;
import copado.onpremise.job.OnPremiseDeploymentJob;
import copado.onpremise.service.credential.CredentialModule;
import copado.onpremise.service.file.FileModule;
import copado.onpremise.service.git.GitModule;
import copado.onpremise.service.salesforce.SalesforceModule;
import copado.onpremise.service.validation.ValidationModule;
import lombok.extern.flogger.Flogger;
import org.apache.commons.cli.*;
import org.apache.commons.configuration.ConfigurationException;

@Flogger
public class Application {

    private static final String OPT_HELP = "help";
    private static final String OPT_DEPLOY_BRANCH_NAME = "deployBranchName";

    public static void main(String[] args) throws ConfigurationException {

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(OPT_HELP, "Show usage information.");
        options.addOption(OPT_DEPLOY_BRANCH_NAME, true, "Branch name of the git repository which contains the Copado deployment branch.");

        try {

            CommandLine line = parser.parse(options, args);

            if (line.hasOption(OPT_DEPLOY_BRANCH_NAME)) {
                String deployBranchName = line.getOptionValue(OPT_DEPLOY_BRANCH_NAME);

                Injector injector = Guice.createInjector(new ConfigurationModule(), new JobModule(), new CredentialModule(), new FileModule(), new GitModule(), new SalesforceModule(), new ValidationModule());
                OnPremiseDeploymentJob job = injector.getInstance(OnPremiseDeploymentJob.class);
                job.setDeployBranchName(deployBranchName);
                job.execute();

            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar copado-deployment-service.jar", options);
            }

        } catch (ParseException e) {
            log.atSevere().log("Could not run copado deployment service. Error: %s", e.getMessage());
        }

    }

}


