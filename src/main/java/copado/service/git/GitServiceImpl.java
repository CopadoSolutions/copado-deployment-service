package copado.service.git;

import copado.util.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("gitService")
@Slf4j
public class GitServiceImpl implements GitService {

    private static final String ORIGIN = "origin/";

    public Git cloneRepo(Path temporalDir) throws GitServiceException {
        if (temporalDir != null) {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(SystemProperties.GIT_URL.value())
                    .setCredentialsProvider(buildCredentialsProvider())
                    .setDirectory(new File(temporalDir.toAbsolutePath().toString()));


            try (Git call = cloneCommand.call()) {
                log.info("Cloned repo:{}", SystemProperties.GIT_URL.value());
                return call;
            } catch (Exception e) {
                log.error("Exception while cloning repo:", e);
                throw new GitServiceException("Could not clone git repository", e);
            }
        }
        throw new GitServiceException("Path to clone git repository can not be null");
    }

    private UsernamePasswordCredentialsProvider buildCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(SystemProperties.GIT_USERNAME.value(), SystemProperties.GIT_PASSWORD.value());
    }

    public void cloneBranchFromRepo(Git git, String branch) throws GitServiceException {
        // Retrieve promote-branch from repo
        log.info("Retrieving branch:{}{}", ORIGIN, branch);
        handleExceptions(() ->
                git.branchCreate()
                        .setName(branch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + branch)
                        .setForce(true).call());
    }

    public Ref getBranch(Git git, String branch) throws GitServiceException {
        // Retrieve latest commit from branch
        log.info("Retrieving id for branch:{}{}", ORIGIN, branch);

        List<Ref> branches = handleExceptions(() -> git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call());
        List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(branch)).collect(Collectors.toList());
        Ref promoteBranchRef = promoteBranchList.get(0);

        log.info("Id:{} for branch:{}{}", promoteBranchRef.getObjectId(), ORIGIN, branch);

        return promoteBranchRef;
    }

    public void mergeWithBranch(Git git, Ref branchToBeMerged, String targetBranch) throws GitServiceException {
        log.info("Merge into target branch, and local commit.");
        handleExceptions(() -> git.merge()
                .include(branchToBeMerged.getObjectId()).setCommit(true).setMessage("Merge branch '" + branchToBeMerged.getName() + "' into '" + targetBranch + "'")
                .call());
    }

    public void checkout(Git git, String branch) throws GitServiceException {
        log.info("Checkout branch:{}", branch);
        handleExceptions(() -> git.checkout().setName(branch).call());
    }

    public void push(Git git) throws GitServiceException {
        log.info("Pushing to remote target branch");
        handleExceptions(() -> git.push().setCredentialsProvider(buildCredentialsProvider()).call());
    }

    private <T> T handleExceptions(GitServiceImpl.GitCodeBlock<T> codeBlock) throws GitServiceException {
        try {
            return codeBlock.execute();
        } catch (Throwable e) {
            log.error("Internal error in git service. Exception: " + e);
            throw new GitServiceException("Internal error in git service", e);
        }
    }

    @FunctionalInterface
    public interface GitCodeBlock<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
