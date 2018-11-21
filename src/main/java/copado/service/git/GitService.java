package copado.service.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Ref;

import java.nio.file.Path;

public interface GitService {

    Git cloneRepo(Path temporalDir) throws GitServiceException;

    void cloneBranchFromRepo(Git git, String branch) throws GitServiceException;

    Ref getBranch(Git git, String branch) throws GitServiceException;

    void mergeWithBranch(Git git, Ref branchToBeMerged, String targetBranch) throws GitServiceException;

    void checkout(Git git, String branch) throws GitServiceException;

    void push(Git git) throws GitServiceException;
}
