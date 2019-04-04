package copado.onpremise.job;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeployRequest {
    private String deploymentJobId;
    private String promoteBranch;
    private String targetBranch;
    private String copadoJobId;
    private String orgDestId;
    private String testLevel;
    private List<String> testClasses = new ArrayList<>();
    private boolean isCheckOnly;
    private String gitAuthor;
    private String gitAuthorEmail;
    private List<String> artifactRepositoryIds = new ArrayList<>();
}

