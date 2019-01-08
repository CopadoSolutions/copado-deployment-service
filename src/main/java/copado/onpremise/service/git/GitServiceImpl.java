package copado.onpremise.service.git;


import com.google.inject.Inject;
import com.google.inject.Provider;
import copado.onpremise.configuration.ApplicationConfiguration;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__({ @Inject}))
@Flogger
class GitServiceImpl implements GitService {

    private static final String ORIGIN = "origin/";

    private ApplicationConfiguration config;

    public GitSession cloneRepo(Path temporalDir) throws GitServiceException {

        log.atInfo().log("Cloning git repository ...");
        if (temporalDir != null) {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(config.getGitUrl())
                    .setCredentialsProvider(buildCredentialsProvider())
                    .setDirectory(new File(temporalDir.toAbsolutePath().toString()));


            try (Git call = cloneCommand.call()) {

                log.atInfo().log("Cloned repo:%s", config.getGitUrl());
                log.atInfo().log("Repository cloned!");
                return new GitSession(call, temporalDir);

            } catch (Exception e) {
                log.atSevere().log("Exception while cloning repo:", e);
                throw new GitServiceException("Could not clone git repository", e);
            }
        }
        throw new GitServiceException("Path to clone git repository can not be null");
    }

    private UsernamePasswordCredentialsProvider buildCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(config.getGitUsername(), config.getGitPassword());
    }

    public void cloneBranchFromRepo(GitSession session, String branch) throws GitServiceException {
        // Retrieve promote-branch from repo
        log.atInfo().log("Retrieving branch:%s%s", ORIGIN, branch);
        handleExceptions(() ->
                session.getGit().branchCreate()
                        .setName(branch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + branch)
                        .setForce(true).call());
    }


    public Branch getBranch(GitSession session, String branch) throws GitServiceException {
        // Retrieve latest commit from branch
        log.atInfo().log("Retrieving id for branch:%s%s", ORIGIN, branch);

        List<Ref> branches = handleExceptions(() -> session.getGit().branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call());
        List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(branch)).collect(Collectors.toList());
        Ref promoteBranchRef = promoteBranchList.get(0);

        log.atInfo().log("Id:%s for branch:%s%s", promoteBranchRef.getObjectId(), ORIGIN, branch);

        return new Branch(promoteBranchRef.getName(), promoteBranchRef.getObjectId());
    }

    public void mergeWithBranch(GitSession session, Branch branchToBeMerged, String targetBranch) throws GitServiceException {
        checkout(session,targetBranch);

        log.atInfo().log("Merge into target branch, and local commit.");
        handleExceptions(() -> session.getGit().merge()
                .include(branchToBeMerged.getId())
                .setCommit(true)
                .setMessage("Merge branch '" + branchToBeMerged.getName() + "' into '" + targetBranch + "'")
                .call());
    }

    public void checkout(GitSession session, String branch) throws GitServiceException {
        log.atInfo().log("Checkout branch:%s", branch);
        handleExceptions(() -> session.getGit().checkout().setName(branch).call());
    }

    public void push(GitSession session) throws GitServiceException {
        log.atInfo().log("Pushing to remote target branch");
        handleExceptions(() -> session.getGit().push().setCredentialsProvider(buildCredentialsProvider()).call());
    }

    private <T> T handleExceptions(GitServiceImpl.GitCodeBlock<T> codeBlock) throws GitServiceException {
        try {
            return codeBlock.execute();
        } catch (Throwable e) {
            log.atSevere().log("Internal error in git service. Exception: " + e);
            throw new GitServiceException("Internal error in git service", e);
        }
    }

    @FunctionalInterface
    public interface GitCodeBlock<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
