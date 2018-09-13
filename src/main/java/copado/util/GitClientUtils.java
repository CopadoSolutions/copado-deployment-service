package copado.util;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class GitClientUtils {

    private GitClientUtils() {
    }

    private static final String ORIGIN = "origin/";


    public static Optional<Git> cloneRepo(Path temporalDir) {
        if (temporalDir != null) {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(SystemProperties.GIT_URL.value())
                    .setCredentialsProvider(buildCredentialsProvider())
                    .setDirectory(new File(temporalDir.toAbsolutePath().toString()));


            try (Git call = cloneCommand.call()) {

                log.info("Cloned repo:{}", SystemProperties.GIT_URL.value());
                return Optional.of(call);
            } catch (Exception e) {
                log.error("Exception while cloning repo:", e);
            }
        }

        return Optional.empty();
    }

    public static UsernamePasswordCredentialsProvider buildCredentialsProvider() {
        return new UsernamePasswordCredentialsProvider(SystemProperties.GIT_USERNAME.value(), SystemProperties.GIT_PASSWORD.value());
    }

    public static void cloneBranchFromRepo(Git git, String branch) throws GitAPIException {
        // Retrieve promote-branch from repo
        log.info("Retrieving branch:{}{}", ORIGIN, branch);
        git.branchCreate()
                .setName(branch)
                .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                .setStartPoint(ORIGIN + branch)
                .setForce(true).call();
    }

    public static Ref getBranch(Git git, String branch) throws GitAPIException {
        // Retrieve latest commit from branch
        log.info("Retrieving id for branch:{}{}", ORIGIN, branch);
        List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
        List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(branch)).collect(Collectors.toList());
        Ref promoteBranchRef = promoteBranchList.get(0);

        log.info("Id:{} for branch:{}{}", promoteBranchRef.getObjectId(), ORIGIN, branch);

        return promoteBranchRef;
    }

    public static void mergeWithBranch(Git git, Ref branchToBeMerged, String targetBranch) throws GitAPIException {
        log.info("Merge into target branch, and local commit.");
        git.merge().include(branchToBeMerged.getObjectId()).setCommit(true).setMessage("Merge branch '" + branchToBeMerged.getName() + "' into '" + targetBranch + "'").call();
    }

    public static void checkout(Git git, String branch) throws GitAPIException {
        log.info("Checkout branch:{}", branch);
        git.checkout().setName(branch).call();
    }

    public static void push(Git git) throws GitAPIException {
        log.info("Pushing to remote target branch");
        git.push().setCredentialsProvider(buildCredentialsProvider()).call();
    }
}
