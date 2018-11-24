package copado.onpremise.service.salesforce;


import com.sforce.soap.metadata.*;
import com.sforce.ws.ConnectionException;
import copado.onpremise.ApplicationConfiguration;
import copado.onpremise.exception.CopadoException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Deploy a zip file of metadata components.
 * Prerequisite: Have a deploy.zip file that includes a package.xml manifest file that
 * details the contents of the zip file.
 */
@Service
@Slf4j
public class SalesforceService {

    // one second in milliseconds
    private static final long ONE_SECOND = 1000;
    // maximum number of attempts to deploy the zip file
    private static final int MAX_NUM_POLL_REQUESTS = 50;

    @Autowired
    private ApplicationConfiguration conf;

    public void deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath) throws IOException, CopadoException, ConnectionException, InterruptedException {

        log.info("Deploying in salesforce zip file:{}", zipFileAbsolutePath);
        byte[] zipBytes = readZipFile(zipFileAbsolutePath);
        DeployOptions deployOptions = new DeployOptions();
        deployOptions.setPerformRetrieve(false);
        deployOptions.setRollbackOnError(true);
        AsyncResult asyncResult = metadataConnection.deploy(zipBytes, deployOptions);
        String asyncResultId = asyncResult.getId();

        // Wait for the deploy to complete
        int poll = 0;
        long waitTimeMilliSecs = ONE_SECOND;
        DeployResult deployResult = null;
        boolean fetchDetails;
        do {
            Thread.sleep(waitTimeMilliSecs);
            // double the wait time for the next iteration
            waitTimeMilliSecs *= 2;
            if (poll++ > MAX_NUM_POLL_REQUESTS) {
                throw new CopadoException("Request timed out. If this is a large set " +
                        "of metadata components, check that the time allowed by " +
                        "MAX_NUM_POLL_REQUESTS is sufficient.");
            }

            // Fetch in-progress details once for every 3 polls
            fetchDetails = (poll % 3 == 0);
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, fetchDetails);

            log.info("Status is: {}", deployResult.getStatus());
            if (!deployResult.isDone() && fetchDetails) {
                printErrors(deployResult, "Failures for deployment in progress:\n");
            }
        }
        while (!deployResult.isDone());

        if (!deployResult.isSuccess() && deployResult.getErrorStatusCode() != null) {
            throw new CopadoException(deployResult.getErrorStatusCode() + " msg: " +
                    deployResult.getErrorMessage());
        }

        if (!fetchDetails) {
            // Get the final result with details if we didn't do it in the last attempt.
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, true);
        }

        if (!deployResult.isSuccess()) {
            printErrors(deployResult, "Final list of failures:\n");
            throw new CopadoException("The files were not successfully deployed");
        }

        log.info("The file {} was successfully deployed", zipFileAbsolutePath);
    }

    /**
     * Read the zip file contents into a byte array.
     *
     * @return byte[]
     * @throws Exception - if cannot find the zip file to deploy
     */
    private byte[] readZipFile(String zipFileAbsolutePath) throws CopadoException, IOException {

        File deployZip = new File(zipFileAbsolutePath);
        if (!deployZip.exists() || !deployZip.isFile()) {
            throw new CopadoException("Cannot find the zip file to deploy. Looking for " + deployZip.getAbsolutePath());
        }

        try (FileInputStream fos = new FileInputStream(deployZip)) {
            try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
                int readbyte = -1;
                while ((readbyte = fos.read()) != -1) {
                    bos.write(readbyte);
                }
                return bos.toByteArray();
            }
        }
    }


    /**
     * Print out any errors, if any, related to the deploy.
     *
     * @param result - DeployResult
     */
    private void printErrors(DeployResult result, String messageHeader) {
        DeployDetails deployDetails = result.getDetails();

        StringBuilder errorMessageBuilder = new StringBuilder();
        if (deployDetails != null) {
            appendErrors(errorMessageBuilder, deployDetails.getComponentFailures());
            appendErrors(errorMessageBuilder, deployDetails.getRunTestResult());
        }

        if (errorMessageBuilder.length() > 0) {
            errorMessageBuilder.insert(0, messageHeader);
            log.error("Error when deploy: {}", errorMessageBuilder.toString());
        }
    }

    private void appendErrors(StringBuilder errorMessageBuilder, DeployMessage[] componentFailures) {
        for (DeployMessage message : componentFailures) {
            String loc = (message.getLineNumber() == 0 ? "" :
                    ("(" + message.getLineNumber() + "," +
                            message.getColumnNumber() + ")"));
            if (loc.length() == 0
                    && !message.getFileName().equals(message.getFullName())) {
                loc = "(" + message.getFullName() + ")";
            }
            errorMessageBuilder.append(message.getFileName() + loc + ":" +
                    message.getProblem()).append('\n');
        }
    }

    private void appendErrors(StringBuilder errorMessageBuilder, RunTestsResult rtr) {
        if (rtr.getFailures() != null) {
            appendErrors(errorMessageBuilder, rtr.getFailures());
        }

        if (rtr.getCodeCoverageWarnings() != null) {
            appendErrors(errorMessageBuilder, rtr.getCodeCoverageWarnings());
        }
    }

    private void appendErrors(StringBuilder errorMessageBuilder, RunTestFailure[] failures) {
        for (RunTestFailure failure : failures) {
            String n = (failure.getNamespace() == null ? "" :
                    (failure.getNamespace() + ".")) + failure.getName();
            errorMessageBuilder.append("Test failure, method: " + n + "." +
                    failure.getMethodName() + " -- " +
                    failure.getMessage() + " stack " +
                    failure.getStackTrace() + "\n\n");
        }
    }

    private void appendErrors(StringBuilder errorMessageBuilder, CodeCoverageWarning[] warnings) {
        for (CodeCoverageWarning ccw : warnings) {
            errorMessageBuilder.append("Code coverage issue");
            if (ccw.getName() != null) {
                String n = (ccw.getNamespace() == null ? "" :
                        (ccw.getNamespace() + ".")) + ccw.getName();
                errorMessageBuilder.append(", class: " + n);
            }
            errorMessageBuilder.append(" -- " + ccw.getMessage() + "\n");
        }
    }


}