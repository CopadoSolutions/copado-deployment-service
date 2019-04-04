package copado.onpremise.connector.salesforce;

import com.sforce.soap.metadata.*;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang.StringUtils;

import static copado.onpremise.connector.salesforce.TipLevel.ERROR;

@Flogger
class DeploymentResultChecker {

    /**
     * @param result
     * @return
     * @throws InvalidDeployResult
     */
    public void fillWithErrorInformation(DeploymentResult deploymentResult, DeployResult result) throws InvalidDeployResult {
        validate(result);
        checkErrorMessage(deploymentResult, result);
        checkComponentFailures(deploymentResult, result.getDetails());
        checkTestFailures(deploymentResult, result.getDetails());
        checkCodeCoverage(deploymentResult, result.getDetails());
    }

    private void checkCodeCoverage(DeploymentResult deploymentResult, DeployDetails details) {
        if (details.getRunTestResult() != null
                && details.getRunTestResult().getCodeCoverageWarnings() != null) {
            for (CodeCoverageWarning ccw : details.getRunTestResult().getCodeCoverageWarnings()) {
                log.atInfo().log("Coverage failure:'%s'", ccw.getName());
                deploymentResult.getTips().add(buildCopadoTip(ERROR,
                        "Code coverage issue: "
                                + (StringUtils.isNotBlank(ccw.getNamespace())
                                ? ccw.getNamespace() + "." : "")
                                + (StringUtils.isNotBlank(ccw.getName()) ? ccw.getName() : "")
                                + " -- " + ccw.getMessage()));
            }
        }
    }

    private void checkTestFailures(DeploymentResult deploymentResult, DeployDetails details) {
        if (details.getRunTestResult() != null && details.getRunTestResult().getFailures() != null) {
            for (RunTestFailure rtf : details.getRunTestResult().getFailures()) {
                log.atInfo().log("Test failure:'%s'", rtf.getName());
                deploymentResult.getTips().add(buildCopadoTip(ERROR,
                        "Test failure, method: "
                                + (rtf.getNamespace() != null ? rtf.getNamespace() + "." : "")
                                + rtf.getName() + "." + rtf.getMethodName() + " -- "
                                + rtf.getMessage() + " stack " + rtf.getStackTrace()));
            }
        }
    }

    private void validate(DeployResult result) throws InvalidDeployResult {
        DeployDetails details = result.getDetails();
        if (details == null) {
            String errorMessage = StringUtils.isNotBlank(result.getErrorMessage()) ? result.getErrorMessage() : "No error message returned by Salesforce metadata api.";
            String exceptionMessage = String.format("Result without details. ErrorMessage: %s", errorMessage);
            log.atSevere().log(exceptionMessage);
            throw new InvalidDeployResult(exceptionMessage);
        }
    }

    private void checkErrorMessage(DeploymentResult deploymentResult, DeployResult result) {
        String errorMessage = result.getErrorMessage();
        StatusCode statusCode = result.getErrorStatusCode();
        if (errorMessage != null) {
            deploymentResult.getTips().add(buildCopadoTip(ERROR, errorMessage));
            log.atInfo().log("General error with code:'%s' and message:'%s'", statusCode, errorMessage);
            deploymentResult.getTips().add(buildCopadoTip(ERROR, "[Copado] Fatal Deployment. See on-premise file for debugging, reproduce error, and share with customer support"));
        }
    }

    private void checkComponentFailures(DeploymentResult deploymentResult, DeployDetails details) {
        for (DeployMessage failure : details.getComponentFailures()) {
            String failureMessage = buildFailureMessage(failure);
            log.atInfo().log("Component failure:'%s'", failureMessage);

            deploymentResult.getTips().add(buildCopadoTip(ERROR, failureMessage));
        }
    }

    private CopadoTip buildCopadoTip(TipLevel tipLevel, String message) {
        return new CopadoTip(ERROR, message, "");
    }

    private String buildFailureMessage(DeployMessage failure) {
        return "[" + failure.getComponentType() + " " + failure.getFullName() + "] " + failure.getProblem();
    }
}
