package copado.onpremise;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@Slf4j
public class Application {

    private static final String OPT_HELP = "help";
    private static final String OPT_DEPLOY_BRANCH_NAME = "deployBranchName";

    private static String deployBranchName;

    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(OPT_HELP, "Show usage information.");
        options.addOption(OPT_DEPLOY_BRANCH_NAME, true, "Branch name of the git repository which contains the Copado deployment branch.");

        try {

            CommandLine line = parser.parse(options, args);

            if (line.hasOption(OPT_DEPLOY_BRANCH_NAME)) {
                deployBranchName = line.getOptionValue(OPT_DEPLOY_BRANCH_NAME);

                SpringApplication.run(Application.class, args);

            } else {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("java -jar copado-deployment-service.jar", options);
            }

        } catch (ParseException e) {
            log.error("Could not run copado deployment service. Error: {}", e.getMessage());
        }

    }

    @Bean
    public String deployBranchName(){
        return deployBranchName;
    }


}


