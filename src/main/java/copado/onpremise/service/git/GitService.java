package copado.onpremise.service.git;

import copado.onpremise.service.credential.GitCredentials;

import java.nio.file.Path;

/**
 * Git client that allows to connect and execute actions in your git repository.
 */
public interface GitService {

    /**
     * Clone the repository under a given temporal directory.
     * This methods uses the credentials for the git repository before cloning it.
     *
     * @param temporalDir
     * @param gitCredentials
     * @return git session which wraps the underliying git implementation session and which references to the base git temporal path
     * @throws GitServiceException
     */
    GitSession cloneRepo(Path temporalDir, GitCredentials gitCredentials) throws GitServiceException;

    /**
     * Force a clone to a remote git branch into
     *
     * @param gitSession
     * @param branch
     * @throws GitServiceException
     */
    void cloneBranchFromRepo(GitSession gitSession, String branch) throws GitServiceException;

    /**
     * Given a branch name  for a remote branch, this method will returns the object representation for this branch
     *
     * @param session
     * @param branch
     * @return
     * @throws GitServiceException
     */
    Branch getBranch(GitSession session, String branch) throws GitServiceException;

    /**
     * Checkouts the targetBranch, and merge the branchToBeMerged into the target branch without fast forward.
     * After that, commits the changes without push.
     *
     * @param gitSession
     * @param branchToBeMerged
     * @param targetBranch
     * @throws GitServiceException
     */
    void mergeWithNoFastForward(GitSession gitSession, Branch branchToBeMerged, String targetBranch) throws GitServiceException;

    /**
     * Checkouts remote branch
     *
     * @param gitSession
     * @param branch
     * @throws GitServiceException
     */
    void checkout(GitSession gitSession, String branch) throws GitServiceException;

    /**
     * Push current branch into remote repository
     *
     * @param gitSession
     * @throws GitServiceException
     */
    void push(GitSession gitSession) throws GitServiceException;

    /**
     * @param sourceBranch
     * @param targetBranch
     * @return <b>true</b> if both branches has the same files and commits<br/>
     * <b>false</b> in other case
     * @throws GitServiceException
     */
    boolean hasDifferences(GitSession session, String sourceBranch, String targetBranch) throws GitServiceException;

    /**
     * @param session
     * @return Head identifier for current head on local directory
     * @throws GitServiceException
     */
    String getHead(GitSession session) throws GitServiceException;

    void fetchBranch(GitSession session, String branchName) throws GitServiceException;

    void commit(GitSession session, String message, String author, String authorEmail) throws GitServiceException;

}
