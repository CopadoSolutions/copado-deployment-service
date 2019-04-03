package copado.onpremise.service.git;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GitDataSet {
    private String correctFileNameInMaster = "README.md";
    private String correctDeploymentBranchLocalName = "deployment/TEST";
    private String correctFileNameInDeploymentBranch = "new_file.md";
    private String correctHeadInDeploymentBranch = "54365d7e99a4f1465bf04e7afff4107510ff404d";
    private String correctMasterBranchLocalName = "master";
    private String correctAuthor = "TEST_AUTHOR";
    private String correctAuthorEmail = "TEST_AUTHOR@email.com";
}
