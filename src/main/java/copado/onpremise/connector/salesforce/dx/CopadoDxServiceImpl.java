package copado.onpremise.connector.salesforce.dx;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.apache.commons.lang.StringUtils;

@Flogger
@AllArgsConstructor(onConstructor = @__({@Inject}))
class CopadoDxServiceImpl implements CopadoDxService {

    public boolean isDxSource(String sourceBranch, String sourceOrgId) {
        if (StringUtils.isNotBlank(sourceOrgId)) {
            String projectDxBranchName = "project/DX-" + sourceOrgId.substring(3, Math.min(15, sourceOrgId.length()));
            return projectDxBranchName.equals(sourceBranch);
        } else {
            log.atInfo().log("Invalid source org identifier: '" + sourceOrgId + "'");
            return false;
        }
    }

}
