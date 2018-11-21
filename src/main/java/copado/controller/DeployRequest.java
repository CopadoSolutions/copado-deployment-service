package copado.controller;

import lombok.Data;

@Data
public class DeployRequest {
    private String deploymentJobId;
    private String promoteBranch;
    private String targetBranch;
    private String deploymentBranch;
    private String gerritChangeId;
    private String copadoJobId;
    private String orgDestId;
}
