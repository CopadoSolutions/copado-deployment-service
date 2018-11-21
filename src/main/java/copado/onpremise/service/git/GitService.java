package copado.onpremise.service.git;

import java.nio.file.Path;

public interface GitService {

    GitSession cloneRepo(Path temporalDir) throws GitServiceException;

    void cloneBranchFromRepo(GitSession gitSession, String branch) throws GitServiceException;

    Branch getBranch(GitSession gitSession, String branch) throws GitServiceException;

    void mergeWithBranch(GitSession gitSession, Branch branchToBeMerged, String targetBranch) throws GitServiceException;

    void checkout(GitSession gitSession, String branch) throws GitServiceException;

    void push(GitSession gitSession) throws GitServiceException;
}
