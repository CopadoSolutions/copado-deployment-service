package copado.onpremise.service.git;

import java.nio.file.Path;

/**
 * Git client that allows to connect and execute actions in your git repository.
 */
public interface GitService {

    /**
     * Clone the repository under a given temporal directory.
     * This methods retrieves the credentials for the git repository before cloning it.
     * @param temporalDir
     * @return git session which wraps the underliying git implementation session and which references to the base git temporal path
     * @throws GitServiceException
     */
    GitSession cloneRepo(Path temporalDir) throws GitServiceException;

    /**
     * Force a clone to a remote git branch into
     * @param gitSession
     * @param branch
     * @throws GitServiceException
     */
    void cloneBranchFromRepo(GitSession gitSession, String branch) throws GitServiceException;

    /**
     * Given a branch name  for a remote branch, this method will returns the object representation for this branch
     * @param session
     * @param branch
     * @return
     * @throws GitServiceException
     */
    Branch getBranch(GitSession session, String branch) throws GitServiceException;

    /**
     * Checkouts the targetBranch, and merge the branchToBeMerged into the target branch.
     * After that, commits the changes without push.
     * @param gitSession
     * @param branchToBeMerged
     * @param targetBranch
     * @throws GitServiceException
     */
    void mergeWithBranch(GitSession gitSession, Branch branchToBeMerged, String targetBranch) throws GitServiceException;

    /**
     * Checkouts remote branch
     * @param gitSession
     * @param branch
     * @throws GitServiceException
     */
    void checkout(GitSession gitSession, String branch) throws GitServiceException;

    /**
     * Push current branch into remote repository
     * @param gitSession
     * @throws GitServiceException
     */
    void push(GitSession gitSession) throws GitServiceException;
}
