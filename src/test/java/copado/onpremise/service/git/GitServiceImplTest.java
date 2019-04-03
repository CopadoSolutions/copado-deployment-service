package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Flogger
public class GitServiceImplTest {

    private GitService gitService;
    private GitSession gitSession;
    private GitTestFactory gitTestContext;

    @Before
    public void setUp() throws IOException, GitServiceException {
        gitTestContext = new GitTestFactory();
        gitTestContext.setCurrentBaseGitDir(gitTestContext.currentBaseGitDir());
        gitService = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());
        gitSession = gitService.cloneRepo(gitTestContext.currentBaseGitDir(), GitCredentialTestFactory.buildCorrectCredentials(gitTestContext.currentTestFolder()));
    }

    @After
    public void tearDown() throws Exception {
        gitService.close(gitSession);
        gitService = null;
        FileUtils.deleteDirectory(gitTestContext.currentBaseGitDir().toFile());
    }


    @Test
    public void clone_repositoryWithCorrectFile() {
        assertTrue(gitTestContext.correctFileInMaster().isFile());
        assertTrue(gitTestContext.correctFileInMaster().exists());
    }

    @Test
    public void test_cloneBranchFromRepo() throws Exception {

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertTrue(gitTestContext.correctGitRefsHeadFilehOfDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctGitRefsHeadFilehOfDeploymentBranch().exists());
    }

    @Test
    public void test_checkout() throws Exception {

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        gitService.checkout(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertTrue(gitTestContext.correctFileInDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctFileInDeploymentBranch().exists());
    }

    @Test
    public void test_getBranch() throws Exception {

        Branch givenBranch = gitService.getBranch(gitSession, gitTestContext.correctDeploymentBranchLocalName());

        assertThat(gitTestContext.currentFetchHeadFileContent(), containsString(gitTestContext.correctHeadInDeploymentBranch()));
        assertThat(gitTestContext.currentFetchHeadFileContent(), containsString(gitTestContext.correctDeploymentBranchLocalName()));
        assertThat(givenBranch.getIdentifier(), is(equalTo(gitTestContext.correctHeadInDeploymentBranch())));
        assertThat(givenBranch.getName(), is(equalTo(gitTestContext.correctDeploymentBranchRefsRemoteName())));

    }

    @Test
    public void test_merge() throws Exception {

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        Branch deploymentBranch = gitService.getBranch(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        gitService.mergeWithNoFastForward(gitSession, deploymentBranch, gitTestContext.correctMasterBranchLocalName());

        final String currentHeadFileFirstLine = gitTestContext.currentHeadFirstLineInFile();

        assertThat(gitTestContext.correctHeadFirstLineAsMaster(), is(equalTo(currentHeadFileFirstLine)));
        assertTrue(gitTestContext.correctFileInDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctFileInDeploymentBranch().exists());
    }


    @Test
    public void test_commit_after_Merge() throws Exception {
        final String expectedMessage = "NEW MESSAGE ADDED IN TEST COMMIT";

        gitService.cloneBranchFromRepo(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        Branch deploymentBranch = gitService.getBranch(gitSession, gitTestContext.correctDeploymentBranchLocalName());
        gitService.mergeWithNoFastForward(gitSession, deploymentBranch, gitTestContext.correctMasterBranchLocalName());
        gitService.commit(gitSession, expectedMessage, gitTestContext.correctAuthor(), gitTestContext.correctAuthorEmail());

        assertThat(gitTestContext.currentHeadFirstLineInFile(), is(equalTo(gitTestContext.correctHeadFirstLineAsMaster())));
        assertThat(gitTestContext.currentCommitEditMsgFistLineInFile(), is(equalTo(expectedMessage)));
        assertTrue(gitTestContext.correctFileInDeploymentBranch().isFile());
        assertTrue(gitTestContext.correctFileInDeploymentBranch().exists());
        assertThat(gitTestContext.currentFetchHeadLinesFileFilteredByMaster(), not(startsWith(gitTestContext.currentRefsHeadsMasterFirstLineInFile())));
    }



}