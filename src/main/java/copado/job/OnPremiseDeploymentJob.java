package copado.job;

import com.sforce.soap.metadata.DeployOptions;
import com.sforce.soap.metadata.MetadataConnection;
import com.sforce.soap.metadata.TestLevel;
import com.sforce.soap.partner.PartnerConnection;
import com.sun.deploy.ref.Helpers;
import copado.util.PathUtils;
import copado.util.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;
import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.CreateBranchCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@Scope("prototype")
public class OnPremiseDeploymentJob {

    private static final String TEMP = "git";
    private static final String ORIGIN = "origin/";


    @Async
    public void doJob(String promoteBranch, String targetBranch) {
        log.info("Starting job");

        Path temporalDir = null;
        try {
            temporalDir = Files.createTempDirectory(TEMP);
            log.info("Created temporal dir:'{}'", temporalDir);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        try {
            Optional<Git> gitOpt = cloneRepo(temporalDir);
            if (gitOpt.isPresent()) {

                log.info("Repository correctly cloned.");
                Git git = gitOpt.get();

                // Retrieve promote-branch from repo
                log.info("Retrieving branch:{}{}", ORIGIN, promoteBranch);
                git.branchCreate()
                        .setName(promoteBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + promoteBranch)
                        .setForce(true).call();

                // Retrieve target-branch from repo
                log.info("Retrieving branch:{}{}", ORIGIN, targetBranch);
                git.branchCreate()
                        .setName(targetBranch)
                        .setUpstreamMode(CreateBranchCommand.SetupUpstreamMode.SET_UPSTREAM)
                        .setStartPoint(ORIGIN + targetBranch)
                        .setForce(true).call();

                // Checkout target_branch
                log.info("Checkout branch:{}{}", ORIGIN, targetBranch);
                git.checkout().setName(targetBranch).call();


                // Retrieve latest commit from branch
                log.info("Retrieving id for branch:{}{}", ORIGIN, promoteBranch);
                List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
                List<Ref> promoteBranchList = branches.stream().filter(b -> b.getName().endsWith(promoteBranch)).collect(Collectors.toList());
                Ref promoteBranchRef = promoteBranchList.get(0);

                // RevWalk walk = new RevWalk( git.getRepository() );
                // RevCommit commitRev = walk.parseCommit(promoteBranchRef.getObjectId());
                // commitRev.getId();

                // Merge promote branch in target branch.

                log.info("Id:{} for branch:{}{}", promoteBranchRef.getObjectId(), ORIGIN, promoteBranch);
                log.info("Merge promote branch into target branch, and local commit.");
                git.merge().include(promoteBranchRef.getObjectId()).setCommit(true).setMessage("Merge branch '" + promoteBranch + "' into '" + targetBranch + "'").call();

                log.info("Pushing to remote target branch");
                git.push().setCredentialsProvider(buildCredentialsProvider()).call();

            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            PathUtils.safeDelete(temporalDir);

        }
    }

    private static UsernamePasswordCredentialsProvider buildCredentialsProvider(){
        return new UsernamePasswordCredentialsProvider(SystemProperties.GIT_USERNAME.value(), SystemProperties.GIT_PASSWORD.value());
    }

    private static Optional<Git> cloneRepo(Path temporalDir) {
        if (temporalDir != null) {
            CloneCommand cloneCommand = Git.cloneRepository()
                    .setURI(SystemProperties.GIT_URL.value())
                    .setCredentialsProvider(buildCredentialsProvider())
                    .setDirectory(new File(temporalDir.toAbsolutePath().toString()));


            try (Git call = cloneCommand.call()) {

                log.info("Cloned repo:{}", SystemProperties.GIT_URL.value());
                return Optional.of(call);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return Optional.empty();
    }

    public Optional<String> retrieveLatestCommitId(Git git) {
        RevCommit youngestCommit = null;

        try {
            List<Ref> branches = git.branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            try (RevWalk walk = new RevWalk(git.getRepository())) {

                for (Ref branch : branches) {
                    RevCommit commit = walk.parseCommit(branch.getObjectId());
                    if (youngestCommit == null) {
                        youngestCommit = commit;
                    } else if (commit.getAuthorIdent().getWhen().compareTo(youngestCommit.getAuthorIdent().getWhen()) > 0) {
                        youngestCommit = commit;
                    }
                }
            }

        } catch (Exception e) {
            log.error("Error retrieving latest commit id:'{}'", e);
        }
        if (youngestCommit != null) {
            return Optional.of(youngestCommit.getId().toString());
        }
        return Optional.empty();
    }
/**
 public static String antExecuteAsync(Project prj, String target, String validationID)
 throws Exception {

 boolean retry = true;
 int retryCount = 0;

 TestLevel testLevel = TestLevel.NoTestRun;
 while (retry) {
 retryCount++;
 retry = false;
 MetadataConnection mc;
 try {
 log.info("Ant execute async:'{}'", prj.getBaseDir() != null ? prj.getBaseDir().toString() : "-");
 boolean isCheckOnly = prj.getProperty("sf.checkOnly").equalsIgnoreCase("true");
 boolean runAllTests = prj.getProperty("sf.runAllTests").equalsIgnoreCase("true");

 if (target.equalsIgnoreCase("deploy")) {
 CopadoMetadataImpl copado = new CopadoMetadataImpl();

 String url = getUrlFromAnt(prj.getProperty("sf.toUrl"), getOrgIdFromSessionId(prj.getProperty("sf.toSid")));
 mc = copado.createMetaDataConnection(prj.getProperty("sf.toSid"), url);

 if (validationID != null && !isCheckOnly) {
 log.info("Quick Deploy:'{}'", validationID);
 try {
 return mc.deployRecentValidation(validationID);
 } catch (Exception e) {
 throw new Exception("Invalid Validation ID. Quick deploy not possible");
 }
 }

 File zipFile = new File(prj.getBaseDir() + "/retrieve.zip");
 File dir2zip = new File(prj.getBaseDir() + "/retrieve");

 Zip.zip(dir2zip, zipFile);
 byte zipBytes[] = readZipFile(zipFile);
 DeployOptions dop = new DeployOptions();
 dop.setCheckOnly(isCheckOnly);
 dop.setPerformRetrieve(false);
 dop.setIgnoreWarnings(true);
 dop.setRollbackOnError(true);
 if (runAllTests) {
 dop.setTestLevel(TestLevel.RunLocalTests);
 log.info("Set TestLevel = RunLocalTests");
 } else {
 // do not specified No Test Run. leave that to
 // salesforce defaults depending on the environment.
 if (testLevel != TestLevel.NoTestRun) {
 dop.setTestLevel(testLevel);
 log.info("Set TestLevel:'{}'", testLevel);
 }

 // if only specified is chosen, search for test classes withing the being deployed classes
 if (testLevel == TestLevel.RunSpecifiedTests) {
 String[] testClasses = null;
 File clsDir = new File(prj.getBaseDir() + "/retrieve/classes");
 if (clsDir.exists()) {
 testClasses = Helpers.findTestClassesOnDirectory(clsDir);
 if (testClasses != null && testClasses.length > 0) {
 log.info("Requesting Fast Deploy with Test Classes:'{}'", StringUtils.join(testClasses, ", "));
 dop.setRunTests(testClasses);
 }
 }

 // Check run tests from attachment
 if (StringUtils.isNotBlank(prj.getProperty("deployment.id")) && StringUtils.isNotBlank(prj.getProperty("sf.copadoSid"))
 && StringUtils.isNotBlank(prj.getProperty("sf.copadoUrl"))) {

 log.info("Getting test classes from attachment ...");

 try {

 PartnerConnection copadoPartnerConnection = copado.createPartnerConnection(prj.getProperty("sf.copadoSid"), prj.getProperty("sf.copadoUrl"));

 String[] testClassesInAttachment = Helpers.findTestClassesInAttachment(copadoPartnerConnection, prj.getProperty("deployment.id"));

 if (testClassesInAttachment != null && testClassesInAttachment.length > 0) {
 log.info("Found classes in attachment:'{}'", StringUtils.join(testClassesInAttachment, ", "));
 dop.setRunTests(ArrayUtils.addAll(testClasses, testClassesInAttachment));
 }

 } catch (Exception e) {
 log.error("Error trying to get test classes from attachment with message:'{}'", e.getMessage(), e);
 }
 }
 }
 }

 dop.setSinglePackage(true);

 AsyncResult asyncResult = mc.deploy(zipBytes, dop);
 return asyncResult.getId();
 }
 } catch (Exception e) {

 if (e.getMessage() != null
 && e.getMessage().contains("testLevel of NoTestRun cannot be used in production")) {
 log.info("Forcing Run All Tests");
 prj.setProperty("sf.runAllTests", "true");
 if (retryCount < 2)
 retry = true;
 } else {
 log.error("Error executing async ant operation with message:'{}'", e.getMessage(), e);
 throw e;
 }
 }
 }

 return null;
 }
 **/

}
