package copado.onpremise.job;

import lombok.Data;

import java.util.List;

@Data
public class DeployRequest {
    private String deploymentJobId;
    private String promoteBranch;
    private String targetBranch;
    private String copadoJobId;
    private String orgDestId;
    private String testLevel;
    private List<String> testClasses;
    private boolean isCheckOnly;
    private String gitAuthor;
    private String gitAuthorEmail;
    private List<String> artifactRepositoryIds;

}

