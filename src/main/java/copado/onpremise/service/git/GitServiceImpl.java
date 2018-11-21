package copado.onpremise.service.git;

import copado.onpremise.ApplicationConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@Service("gitService")
@Slf4j
class GitServiceImpl implements GitService {

    @Autowired
    private ApplicationConfiguration config;

    @Autowired
    private ApplicationContext ctx;

    private static final String ORIGIN = "origin/";


    public GitSession cloneRepo(Path temporalDir) throws GitServiceException {
        if (temporalDir != null) {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(config.getGitUrl())
                    .setCredentialsProvider(buildCredentialsProvider())
                    .setDirectory(new File(temporalDir.toAbsolutePath().toString()));


            try (Git call = cloneCommand.call()) {
                log.info("Cloned repo:{}", config.getGitUrl());
                GitSessionImpl gitSession = ctx.getBean(GitSessionImpl.class);
                gitSession.setGit(call);
                log.info("Repository cloned!");
                return gitSession;

            } catch (Exception e) {
                log.error("Exception while cloning repo:", e);
                throw new GitServiceException("Could not clone git repository", e);
            }
        }
        throw new GitServiceException("Path to clone git repository can not be null");
    }

    private UsernamePasswordCredentialsProvider buildCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(config.getGitUsername(), config.getGitPassword());
    }

    public void cloneBranchFromRepo(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        // Retrieve promote-branch from repo
        log.info("Retrieving branch:{}{}", ORIGIN, branch);
        handleExceptions(() ->
                gitSession.getGit().branchCreate()
                        .setName(branch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + branch)
                        .setForce(true).call());
    }

    public Branch getBranch(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        // Retrieve latest commit from branch
        log.info("Retrieving id for branch:{}{}", ORIGIN, branch);

        List<Ref> branches = handleExceptions(() -> gitSession.getGit().branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call());
        List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(branch)).collect(Collectors.toList());
        Ref promoteBranchRef = promoteBranchList.get(0);

        log.info("Id:{} for branch:{}{}", promoteBranchRef.getObjectId(), ORIGIN, branch);

        BranchImpl toBeReturn = ctx.getBean(BranchImpl.class);
        toBeReturn.setName(promoteBranchRef.getName());
        toBeReturn.setId(promoteBranchRef.getObjectId());

        return toBeReturn;
    }

    public void mergeWithBranch(GitSession session, Branch branchToBeMerged, String targetBranch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        BranchImpl branch = castBranch(branchToBeMerged);

        log.info("Merge into target branch, and local commit.");
        handleExceptions(() -> gitSession.getGit().merge()
                .include(branch.getId()).setCommit(true).setMessage("Merge branch '" + branch.getName() + "' into '" + targetBranch + "'")
                .call());
    }

    public void checkout(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        log.info("Checkout branch:{}", branch);
        handleExceptions(() -> gitSession.getGit().checkout().setName(branch).call());
    }

    public void push(GitSession session) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        log.info("Pushing to remote target branch");
        handleExceptions(() -> gitSession.getGit().push().setCredentialsProvider(buildCredentialsProvider()).call());
    }

    private GitSessionImpl castSession(GitSession gitSession) throws GitServiceException {
        if (gitSession instanceof GitSessionImpl) {
            return (GitSessionImpl) gitSession;
        }
        throw new GitServiceException("Could not cast git-session to " + GitSessionImpl.class.getCanonicalName());
    }

    private BranchImpl castBranch(Branch branch) throws GitServiceException {
        if (branch instanceof BranchImpl) {
            return (BranchImpl) branch;
        }
        throw new GitServiceException("Could not cast branch to " + BranchImpl.class.getCanonicalName());
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
