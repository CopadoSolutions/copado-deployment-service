package copado.onpremise.service.git;


import com.google.inject.Inject;
import com.google.inject.Provider;
import copado.onpremise.service.credential.GitCredentials;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.jgit.api.*;
import org.eclipse.jgit.api.errors.EmptyCommitException;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.RefSpec;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

@AllArgsConstructor(onConstructor = @__({@Inject}))
@Flogger
class GitServiceImpl implements GitService {

    private static final String ORIGIN = "origin/";
    private static final String REFS_REMOTE_ORIGIN_PREFFIX = "refs/remotes/origin/";
    private static final String GITIGNORE = ".gitignore";

    private Provider<GitSession> gitSessionProvider;
    private Provider<Branch> gitBranchProvider;
    private GitServiceRemote gitServiceRemote;

    @Override
    public GitSession cloneRepo(Path temporalDir, GitCredentials gitCredentials) throws GitServiceException {

        log.atInfo().log("Cloning git repository ...");
        GitSessionImpl gitSession = castSession(gitSessionProvider.get());

        if (temporalDir != null) {
            try (Git call = gitServiceRemote.cloneRepository(gitCredentials.getUrl(), buildCredentialsProvider(gitCredentials), temporalDir)) {

                log.atInfo().log("Cloned repo:%s", gitCredentials.getUrl());
                call.getRepository().getConfig().setBoolean("diff", null, "renames", false);
                call.getRepository().getConfig().save();
                gitSession.setGit(call);
                gitSession.setBaseDir(temporalDir);
                gitSession.setGitCredentials(gitCredentials);
                log.atInfo().log("Repository cloned!");
                return gitSession;

            } catch (Exception e) {
                log.atSevere().log("Exception while cloning repo:", e);
                throw new GitServiceException("Could not clone git repository", e);
            }
        }
        throw new GitServiceException("Path to clone git repository can not be null");
    }

    private UsernamePasswordCredentialsProvider buildCredentialsProvider(GitCredentials gitCredentials) {
        return new UsernamePasswordCredentialsProvider(gitCredentials.getUsername(), gitCredentials.getPassword());
    }

    @Override
    public void cloneBranchFromRepo(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        // Retrieve promote-branch from repo
        log.atInfo().log("Retrieving branch:%s%s", ORIGIN, branch);
        handleExceptions(() ->
                gitSession.getGit().branchCreate()
                        .setName(branch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + branch)
                        .setForce(true).call());
    }


    @Override
    public Branch getBranch(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        BranchImpl toBeReturn = castBranch(gitBranchProvider.get());
        // Retrieve latest commit from branch
        log.atInfo().log("Retrieving id for branch:%s%s", ORIGIN, branch);

        List<Ref> branches = handleExceptions(() -> gitSession.getGit().branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call());
        List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(branch)).collect(Collectors.toList());
        Ref promoteBranchRef = promoteBranchList.get(0);

        log.atInfo().log("Id:%s for branch:%s%s", promoteBranchRef.getObjectId(), ORIGIN, branch);

        toBeReturn.setName(promoteBranchRef.getName());
        toBeReturn.setId(promoteBranchRef.getObjectId());

        return toBeReturn;
    }

    @Override
    public void mergeWithNoFastForward(GitSession session, Branch branchToBeMerged, String targetBranch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        BranchImpl branch = castBranch(branchToBeMerged);

        checkout(session, targetBranch);
        // ObjectId base = getHead(gitSession.getGit()).getId();
        MergeCommand.FastForwardMode fastForwardMode = MergeCommand.FastForwardMode.NO_FF;

        log.atInfo().log("Merge into target branch, and local commit.");
        handleExceptions(() -> gitSession.getGit().merge()
                .include(branch.getId())
                .setCommit(true)
                .setFastForward(fastForwardMode)
                .setMessage("Merge branch '" + branch.getName() + "' into '" + targetBranch + "'")

                .call());

    }

    @Override
    public void checkout(GitSession session, String branch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        log.atInfo().log("Checkout branch:%s", branch);
        handleExceptions(() -> gitSession.getGit().checkout().setName(branch).call());
    }

    @Override
    public void push(GitSession session) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        log.atInfo().log("Pushing to remote target branch");
        handleExceptions(() -> gitSession.getGit().push().setCredentialsProvider(buildCredentialsProvider(session.getGitCredentials())).call());
    }

    @Override
    public boolean hasDifferences(GitSession session, String sourceBranch, String targetBranch) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        log.atInfo().log("Checking differences between branches. Source: %s, Target: %s", sourceBranch, targetBranch);



        handleExceptions(() ->
                gitSession.getGit()
                        .fetch()
                        .setCredentialsProvider(buildCredentialsProvider(session.getGitCredentials()))
                        .setRefSpecs(buildBranchRefSpec(sourceBranch), buildBranchRefSpec(targetBranch))
                        .call());

        String sourceBranchRemoteRef = getRemoteBranchRef(sourceBranch);
        String targetBranchRemoteRef = getRemoteBranchRef(targetBranch);

        AbstractTreeIterator oldTreeParser = prepareTreeParser(gitSession.getGit(), sourceBranchRemoteRef);
        AbstractTreeIterator newTreeParser = prepareTreeParser(gitSession.getGit(), targetBranchRemoteRef);

        List<DiffEntry> diffEntries = handleExceptions(() ->
                gitSession.getGit().diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).call());

        if (diffEntries.size() > 0) {
            log.atInfo().log("Found:'%s' diff entries", diffEntries.size());
            for (DiffEntry de : diffEntries) {
                log.atInfo().log("Diff Entry:'%s' - New path:'%s' - Old path:'%s'", de.getChangeType(), de.getNewPath(), de.getOldPath());
            }
            return true;
        }
        return false;

    }

    @Override
    public String getHead(GitSession session) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        return handleExceptions(() -> gitSession.getGit().log().call().iterator().next()).getId().getName();
    }

    @Override
    public void fetchBranch(GitSession session, String branchName) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);

        RefSpec spec = buildBranchRefSpec(branchName);

        handleExceptions(() ->
                gitSession.getGit()
                        .fetch()
                        .setCredentialsProvider(buildCredentialsProvider(session.getGitCredentials()))
                        .setRefSpecs(spec)
                        .call());
    }

    @Override
    public void commit(GitSession session, String message, String author, String authorEmail) throws GitServiceException {
        GitSessionImpl gitSession = castSession(session);
        CommitCommand commitCommand = gitSession.getGit().commit()
                .setAllowEmpty(false)
                .setMessage(message);

        if (StringUtils.isNotBlank(author)) {
            commitCommand.setAuthor(author, authorEmail);
            commitCommand.setCommitter(author, authorEmail);
        }

        processGitIgnore(gitSession);

        try {
            commitCommand.call();
        } catch (EmptyCommitException e){
            String errorMessage = String.format("Could not commit with message: '%s'", message);
            log.atWarning().withCause(e).log(errorMessage);
        } catch (GitAPIException e) {
            String errorMessage = String.format("Could not commit with message: '%s'", message);
            log.atSevere().withCause(e).log(errorMessage);
            throw new GitServiceException(errorMessage);
        }


    }

    private void processGitIgnore(GitSessionImpl gitSession) {

        Path gitIgnorePath = gitSession.getBaseDir().resolve(GITIGNORE);
        try {

            if (gitIgnorePath.toFile().isDirectory()) {
                processGitIgnoreDirectory(gitIgnorePath);
            }
            resetFile(gitSession, gitIgnorePath);

        } catch (Exception e) {
            log.atSevere().withCause(e).log("Error processing .gitignore directory");
        }
    }

    private void processGitIgnoreDirectory(Path gitIgnorePath) throws IOException {
        log.atInfo().log(".gitignore folder is not supported.");

        Path gitIgnoreFileInsideGitIgnoreDir = gitIgnorePath.resolve(GITIGNORE);
        if (gitIgnoreFileInsideGitIgnoreDir.toFile().isFile()) {
            processGitIgnoreFolderWithFileInside(gitIgnorePath, gitIgnoreFileInsideGitIgnoreDir);
        } else {
            removeGitIgnoreFolder(gitIgnorePath);
        }
    }

    private void resetFile(GitSessionImpl gitSession, Path gitIgnorePath) throws GitAPIException {
        log.atInfo().log("Reset from index .gitignore file");
        gitSession.getGit().reset().addPath(gitIgnorePath.toString()).call();
    }

    private void removeGitIgnoreFolder(Path gitIgnorePath) throws IOException {
        log.atInfo().log("Removing .gitignore dir");
        FileUtils.deleteDirectory(gitIgnorePath.toFile());
    }

    private void processGitIgnoreFolderWithFileInside(Path gitIgnorePath, Path gitIgnoreFileInsideGitIgnoreDir) throws IOException {
        log.atInfo().log("Processing .gitignore/.gitignore file");

        List<String> lines = Files.readAllLines(gitIgnoreFileInsideGitIgnoreDir);
        lines.add(GITIGNORE);
        removeGitIgnoreFolder(gitIgnorePath);
        Files.write(gitIgnorePath, lines);
    }

    private String getRemoteBranchRef(String branch) {
        return REFS_REMOTE_ORIGIN_PREFFIX + branch;
    }

    private RefSpec buildBranchRefSpec(String branch){
        return new RefSpec().setSourceDestination("refs/heads/" + branch, "refs/remotes/" + ORIGIN + branch);
    }

    private AbstractTreeIterator prepareTreeParser(Git git, String ref) throws GitServiceException {
        // from the commit we can build the tree which allows us to construct
        // the TreeParser
        Ref head = handleExceptions(() -> git.getRepository().exactRef(ref));
        try (RevWalk walk = new RevWalk(git.getRepository())) {
            RevCommit commit = handleExceptions(() -> walk.parseCommit(head.getObjectId()));
            RevTree tree = handleExceptions(() -> walk.parseTree(commit.getTree().getId()));

            CanonicalTreeParser oldTreeParser = new CanonicalTreeParser();
            try (ObjectReader oldReader = git.getRepository().newObjectReader()) {
                oldTreeParser.reset(oldReader, tree.getId());
            } catch (IOException e) {
                log.atSevere().withCause(e).log("Internal error in git service.");
                throw new GitServiceException("Internal error in git service", e);
            }

            walk.dispose();

            return oldTreeParser;
        }
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
            log.atSevere().withCause(e).log("Internal error in git service.");
            throw new GitServiceException("Internal error in git service", e);
        }
    }

    @FunctionalInterface
    public interface GitCodeBlock<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
