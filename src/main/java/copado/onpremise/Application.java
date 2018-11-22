package copado.onpremise;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.nio.file.Path;
import java.nio.file.Paths;

@SpringBootApplication
@Slf4j
public class Application {

    private static final String OPT_HELP = "help";
    private static final String OPT_PAYLOAD_PATH = "payloadDir";

    private static String payloadDirPath;

    public static void main(String[] args) {

        CommandLineParser parser = new DefaultParser();

        Options options = new Options();
        options.addOption(OPT_HELP, "Show usage information");
        options.addOption(OPT_PAYLOAD_PATH, true, "Directory which contains all the files needed to run the Job.");

        try {

            CommandLine line = parser.parse(options, args);

            if (line.hasOption(OPT_PAYLOAD_PATH)) {
                payloadDirPath = line.getOptionValue(OPT_PAYLOAD_PATH);

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
    public Path payloadDirPath(){
        return Paths.get(payloadDirPath);
    }


}


