package copado.onpremise.connector.salesforce;


import com.google.inject.Inject;
import com.google.inject.Provider;
import com.sforce.soap.metadata.*;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.soap.partner.QueryResult;
import com.sforce.soap.partner.SaveResult;
import com.sforce.soap.partner.sobject.SObject;
import com.sforce.ws.ConnectionException;
import copado.onpremise.configuration.ApplicationConfiguration;
import copado.onpremise.exception.CopadoException;
import copado.onpremise.job.DeployRequest;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


@AllArgsConstructor(onConstructor = @__({@Inject}))
@Flogger
public class SalesforceServiceImpl implements SalesforceService {

    // one second in milliseconds
    private static final long ONE_SECOND = 1000;
    // maximum number of attempts to deploy the zip file
    private static final int MAX_NUM_POLL_REQUESTS = 50;

    private Provider<DeploymentResultChecker> copadoTipFactoryProvider;

    private ApplicationConfiguration conf;

    public DeploymentResult deployZip(MetadataConnection metadataConnection, String zipFileAbsolutePath, DeployRequest deployRequest, SalesforceDeployerDelegate delegate) throws CopadoException {


        log.atInfo().log("Deploying in salesforce zip file:%s", zipFileAbsolutePath);
        byte[] zipBytes = readZipFile(zipFileAbsolutePath);
        DeployOptions deployOptions = new DeployOptions();
        deployOptions.setIgnoreWarnings(true);
        deployOptions.setSinglePackage(true);
        deployOptions.setPerformRetrieve(false);
        deployOptions.setRollbackOnError(true);
        deployOptions.setCheckOnly(deployRequest.isCheckOnly());
        deployOptions.setTestLevel(TestLevelBuilder.build(deployRequest.getTestLevel()));
        setRunTests(deployRequest, deployOptions);

        AsyncResult asyncResult = deployMetadata(metadataConnection, zipBytes, deployOptions);
        String asyncResultId = asyncResult.getId();
        delegate.notifyAsyncId(asyncResultId);

        // Wait for the deploy to complete
        int poll = 0;
        long waitTimeMilliSecs = ONE_SECOND;
        DeployResult deployResult;
        boolean fetchDetails;
        do {
            idle(waitTimeMilliSecs);
            // double the wait time for the next iteration
            waitTimeMilliSecs *= 2;
            if (poll++ > MAX_NUM_POLL_REQUESTS) {
                throw new CopadoException("Request timed out. If this is a large set " +
                        "of metadata components, check that the time allowed by " +
                        "MAX_NUM_POLL_REQUESTS is sufficient.");
            }

            // Fetch in-progress details once for every 3 polls
            fetchDetails = (poll % 3 == 0);
            deployResult = checkDeployStatus(metadataConnection, asyncResultId, fetchDetails);

            log.atInfo().log("Status is: %s", deployResult.getStatus());
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
            deployResult = checkDeployStatus(metadataConnection, asyncResultId, true);
        }

        return buildDeploymentResult(asyncResultId, deployResult);
    }

    private DeploymentResult buildDeploymentResult(String asyncId, DeployResult deployResult) throws CopadoException {

        log.atInfo().log("Started building deployment result");

        DeploymentResult deploymentResult = new DeploymentResult();
        deploymentResult.setAsyncId(asyncId);
        deploymentResult.setSuccess(deployResult.isSuccess());

        if (!deployResult.isSuccess()) {
            printErrors(deployResult, "Final list of failures:\n");
            DeploymentResultChecker deploymentResultChecker = copadoTipFactoryProvider.get();
            deploymentResultChecker.fillWithErrorInformation(deploymentResult, deployResult);
        }

        log.atInfo().log("Finished building deployment result: %s", deploymentResult);
        return deploymentResult;

    }

    private void setRunTests(DeployRequest deployRequest, DeployOptions deployOptions) throws CopadoException {
        if (deployOptions.getTestLevel() == TestLevel.RunSpecifiedTests) {
            if (deployRequest.getTestClasses() == null) {
                throw new CopadoException("Test classes not found and they are needed in order to run specified tests.");
            }
            deployOptions.setRunTests(deployRequest.getTestClasses().toArray(new String[0]));
        }
    }

    private AsyncResult deployMetadata(MetadataConnection metadataConnection, byte[] zipBytes, DeployOptions deployOptions) throws CopadoException {
        AsyncResult asyncResult;
        try {
            asyncResult = metadataConnection.deploy(zipBytes, deployOptions);
        } catch (ConnectionException e) {
            log.atSevere().withCause(e).log("Could not deploy metadata");
            throw new CopadoException("Could not deploy metadata");
        }
        return asyncResult;
    }

    private DeployResult checkDeployStatus(MetadataConnection metadataConnection, String asyncResultId, boolean fetchDetails) throws CopadoException {
        DeployResult deployResult;
        try {
            deployResult = metadataConnection.checkDeployStatus(asyncResultId, fetchDetails);
        } catch (ConnectionException e) {
            log.atSevere().withCause(e).log("Could not check deploy status: %s", asyncResultId);
            throw new CopadoException(String.format("Could not check deploy status: %s", asyncResultId));
        }
        return deployResult;
    }

    private void idle(long waitTimeMilliSecs) throws CopadoException {
        try {
            Thread.sleep(waitTimeMilliSecs);
        } catch (InterruptedException e) {
            log.atSevere().withCause(e).log("Process interrupted while waiting for deployment completion.");
            throw new CopadoException("Process interrupted while waiting for deployment completion.");
        }
    }

    @Override
    public List<SObject> query(PartnerConnection partnerConnection, String query) throws CopadoException {

        List<SObject> records = new ArrayList<>();
        QueryResult qResult;
        try {
            log.atInfo().log("Starting to query into Salesforce: %s", query);
            qResult = partnerConnection.query(query);
            boolean done = false;

            log.atInfo().log("Result size: %s", qResult.getSize());
            if (qResult.getSize() > 0) {
                while (!done) {
                    records.addAll(Arrays.asList(qResult.getRecords()));

                    done = qResult.isDone();
                    if (!done) {
                        qResult = partnerConnection.queryMore(qResult.getQueryLocator());
                    }
                }
            }

            log.atInfo().log("Finished to query into Salesforce: %s", query);
        } catch (ConnectionException ce) {
            log.atSevere().withCause(ce).log("Could not execute query into Salesforce: %s", query);
            throw new CopadoException("Could not execute query into Salesforce: " + query);
        }

        return records;
    }

    @Override
    public void updateStringField(PartnerConnection partnerConnection, String id, String objectType, String fieldName, String value) throws CopadoException {
        log.atInfo().log("Updating salesforce object: '%s', field: '%s', id: '%s', new value: '%s'", objectType, fieldName, id, value);

        if (id == null) {
            log.atSevere().log("Unable to update field, invalid identifier.");
            throw new CopadoException("Unable to update field, invalid identifier.");
        }

        SObject object = new SObject();
        object.setType(objectType);
        object.setField(fieldName, value);
        object.setId(id);

        try {
            SaveResult[] resultArr = partnerConnection.update(new SObject[]{object});
            if (resultArr == null || resultArr.length <= 0) {
                throw new CopadoException("Not result found for update");
            }

            log.atInfo().log("Could update status:'%s', result:'%s'", resultArr[0].getSuccess(), resultArr);

        } catch (Exception e) {
            log.atSevere().withCause(e).log("Error updating: '%s.%s' with id: '%s'", objectType, fieldName, id);
            throw new CopadoException(String.format("Error updating: '%s.%s' with id: '%s'", objectType, fieldName, id));
        }
    }

    /**
     * Read the zip file contents into a byte array.
     *
     * @return byte[]
     * @throws Exception - if cannot find the zip file to deploy
     */
    private byte[] readZipFile(String zipFileAbsolutePath) throws CopadoException {

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
        } catch (IOException e) {
            log.atSevere().withCause(e).log("Error while reading zip file: '%s'", zipFileAbsolutePath);
            throw new CopadoException(String.format("Error while reading zip file: '%s'", zipFileAbsolutePath));
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
            log.atSevere().log("Error when deploy: %s", errorMessageBuilder.toString());
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

    public String createTxtAttachment(PartnerConnection partnerConnection, String parentId, String attachmentName, String attachmentContent) throws CopadoException {
        log.atInfo().log("Uploading attachment: '%s', into: '%s', with content: '%s'", attachmentName, parentId, attachmentContent);

        String attId = "";
        try {
            SObject att = new SObject("Attachment");
            att.setField("ParentId", parentId);
            att.setField("Name", attachmentName);
            att.setField("Body", attachmentContent.getBytes());
            SaveResult[] srs = partnerConnection.create(new SObject[]{att});
            for (SaveResult sr : srs) {
                if (!sr.isSuccess()) {
                    log.atInfo().log("Could not create Attachment:'%s' for id:'%s' - Error:'%s'", attachmentName, parentId, Arrays.stream(sr.getErrors()).map(error -> error.getMessage()).collect(Collectors.joining(", ")));
                } else {
                    attId = sr.getId();
                }
            }
            log.atInfo().log("Attachment created: '%s'", attId);

            return attId;
        } catch (Exception e) {
            log.atSevere().withCause(e).log("Could not upload attachment");
            throw new CopadoException("Could not upload attachment", e);
        }
    }

}