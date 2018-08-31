package copado.util;


import java.io.*;

import java.rmi.RemoteException;

import com.sforce.soap.enterprise.EnterpriseConnection;
import com.sforce.soap.enterprise.LoginResult;
import com.sforce.soap.metadata.AsyncResult;
import com.sforce.soap.metadata.DeployDetails;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.DeployResult;
import com.sforce.soap.metadata.DeployMessage;
import com.sforce.soap.metadata.RunTestsResult;
import com.sforce.soap.metadata.RunTestFailure;
import com.sforce.soap.metadata.CodeCoverageWarning;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;

/**
 * Deploy a zip file of metadata components.
 * Prerequisite: Have a deploy.zip file that includes a package.xml manifest file that
 * details the contents of the zip file.
 */
public class SalesforceUtils {
    // binding for the metadata WSDL used for making metadata API calls
    private MetadataConnection metadataConnection;

    static BufferedReader rdr = new BufferedReader(new InputStreamReader(System.in));

    private static final String ZIP_FILE = "deploy.zip";

    // one second in milliseconds
    private static final long ONE_SECOND = 1000;
    // maximum number of attempts to deploy the zip file
    private static final int MAX_NUM_POLL_REQUESTS = 50;

    public static void main(String[] args) throws Exception {
        final String USERNAME = "user@company.com";
        // This is only a sample. Hard coding passwords in source files is a bad practice.
        final String PASSWORD = "password";

        //TODO: version como parametro
        final String URL = "https://login.salesforce.com/services/Soap/c/43.0";

        SalesforceUtils sample = new SalesforceUtils(USERNAME, PASSWORD, URL);
        sample.deployZip();
    }

    public SalesforceUtils(String username, String password, String loginUrl)
            throws ConnectionException {
        createMetadataConnection(username, password, loginUrl);
    }

    public void deployZip()
            throws RemoteException, Exception
    {
        byte zipBytes[] = readZipFile();
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
                throw new Exception("Request timed out. If this is a large set " +
                        "of metadata components, check that the time allowed by " +
                        "MAX_NUM_POLL_REQUESTS is sufficient.");
            }

            // Fetch in-progress details once for every 3 polls
            fetchDetails = (poll % 3 == 0);
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, fetchDetails);
            System.out.println("Status is: " + deployResult.getStatus());
            if (!deployResult.isDone() && fetchDetails) {
                printErrors(deployResult, "Failures for deployment in progress:\n");
            }
        }
        while (!deployResult.isDone());

        if (!deployResult.isSuccess() && deployResult.getErrorStatusCode() != null) {
            throw new Exception(deployResult.getErrorStatusCode() + " msg: " +
                    deployResult.getErrorMessage());
        }

        if (!fetchDetails) {
            // Get the final result with details if we didn't do it in the last attempt.
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, true);
        }

        if (!deployResult.isSuccess()) {
            printErrors(deployResult, "Final list of failures:\n");
            throw new Exception("The files were not successfully deployed");
        }

        System.out.println("The file " + ZIP_FILE + " was successfully deployed");
    }

    /**
     * Read the zip file contents into a byte array.
     * @return byte[]
     * @throws Exception - if cannot find the zip file to deploy
     */
    private byte[] readZipFile()
            throws Exception
    {
        // We assume here that you have a deploy.zip file.
        // See the retrieve sample for how to retrieve a zip file.
        File deployZip = new File(ZIP_FILE);
        if (!deployZip.exists() || !deployZip.isFile())
            throw new Exception("Cannot find the zip file to deploy. Looking for " +
                    deployZip.getAbsolutePath());

        FileInputStream fos = new FileInputStream(deployZip);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        int readbyte = -1;
        while ((readbyte = fos.read()) != -1)  {
            bos.write(readbyte);
        }
        fos.close();
        bos.close();
        return bos.toByteArray();
    }


    /**
     * Print out any errors, if any, related to the deploy.
     * @param result - DeployResult
     */
    private void printErrors(DeployResult result, String messageHeader)
    {
        DeployDetails deployDetails = result.getDetails();

        StringBuilder errorMessageBuilder = new StringBuilder();
        if (deployDetails != null) {
            DeployMessage[] componentFailures = deployDetails.getComponentFailures();
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
            RunTestsResult rtr = deployDetails.getRunTestResult();
            if (rtr.getFailures() != null) {
                for (RunTestFailure failure : rtr.getFailures()) {
                    String n = (failure.getNamespace() == null ? "" :
                            (failure.getNamespace() + ".")) + failure.getName();
                    errorMessageBuilder.append("Test failure, method: " + n + "." +
                            failure.getMethodName() + " -- " +
                            failure.getMessage() + " stack " +
                            failure.getStackTrace() + "\n\n");
                }
            }
            if (rtr.getCodeCoverageWarnings() != null) {
                for (CodeCoverageWarning ccw : rtr.getCodeCoverageWarnings()) {
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

        if (errorMessageBuilder.length() > 0) {
            errorMessageBuilder.insert(0, messageHeader);
            System.out.println(errorMessageBuilder.toString());
        }
    }

    private void createMetadataConnection(
            final String username,
            final String password,
            final String loginUrl) throws ConnectionException {

        final ConnectorConfig loginConfig = new ConnectorConfig();
        loginConfig.setAuthEndpoint(loginUrl);
        loginConfig.setServiceEndpoint(loginUrl);
        loginConfig.setManualLogin(true);
        LoginResult loginResult = (new EnterpriseConnection(loginConfig)).login(
                username, password);

        final ConnectorConfig metadataConfig = new ConnectorConfig();
        metadataConfig.setServiceEndpoint(loginResult.getMetadataServerUrl());
        metadataConfig.setSessionId(loginResult.getSessionId());
        this.metadataConnection = new MetadataConnection(metadataConfig);
    }

}