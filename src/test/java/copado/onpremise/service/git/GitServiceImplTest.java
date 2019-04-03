package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static copado.onpremise.service.git.GitTestFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Flogger
public class GitServiceImplTest {

    private GitService git;
    private GitSession session;


    @Before
    public void setUp() throws GitServiceException {
        GitTestFactory.setUp();
        git = new GitServiceImpl(GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());
        session = git.cloneRepo(currentBaseGitDir(), GitCredentialTestFactory.buildCorrectCredentials(currentTestFolder()));
    }

    @After
    public void tearDown() throws Exception {
        git.close(session);
        git = null;

        FileUtils.deleteDirectory(currentBaseGitDir().toFile());
        GitTestFactory.tearDown();
    }


    @Test
    public void cloneMaster_AndCheckExistingFile() {
        assertTrue(correctFileInMaster().isFile());
        assertTrue(correctFileInMaster().exists());
    }

    @Test
    public void cloneMaster_AndCheckThatDeploymentFileDoesNotExists() {
        assertFalse(correctFileInDeploymentBranch().isFile());
        assertFalse(correctFileInDeploymentBranch().exists());
    }

    @Test
    public void cloneBranch_DeploymentTest() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());

        assertTrue(refsHeadOfDeploymentBranch().isFile());
        assertTrue(refsHeadOfDeploymentBranch().exists());
    }

    @Test(expected = GitServiceException.class)
    public void cloneBranch_InvalidBranch() throws Exception {
        git.cloneBranchFromRepo(session, invalidBranchLocalName());
    }

    @Test
    public void checkout_DeploymentTestBranch() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        git.checkout(session, correctDeploymentBranchLocalName());

        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
    }

    @Test(expected = GitServiceException.class)
    public void checkout_InvalidBranch() throws Exception {
        git.checkout(session, invalidBranchLocalName());
    }

    @Test
    public void checkout_CurrentBranch() throws Exception {
        git.checkout(session, correctMasterBranchLocalName());
        assertTrue(correctFileInMaster().isFile());
        assertTrue(correctFileInMaster().exists());
    }

    @Test
    public void getBranch_DeploymentTest() throws Exception {

        Branch givenBranch = git.getBranch(session, correctDeploymentBranchLocalName());

        assertThat(currentContentInFileFetchHead(), containsString(correctHeadInDeploymentBranch()));
        assertThat(currentContentInFileFetchHead(), containsString(correctDeploymentBranchLocalName()));
        assertThat(givenBranch.getIdentifier(), is(equalTo(correctHeadInDeploymentBranch())));
        assertThat(givenBranch.getName(), is(equalTo(correctDeploymentBranchRefsRemoteName())));

    }

    @Test(expected = GitServiceException.class)
    public void getBranch_InvalidBranch() throws Exception {

        git.getBranch(session, invalidBranchLocalName());
    }

    @Test
    public void merge() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        Branch deploymentBranch = git.getBranch(session, correctDeploymentBranchLocalName());
        git.mergeWithNoFastForward(session, deploymentBranch, correctMasterBranchLocalName());

        assertThat(correctMasterRefHead(), is(equalTo(currentFirstLineInFileHead())));
        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
    }

    @Test(expected = GitServiceException.class)
    public void merge_invalidBranch() throws Exception {
        Branch deploymentBranch = git.getBranch(session, correctMasterBranchLocalName());
        git.mergeWithNoFastForward(session, deploymentBranch, invalidBranchLocalName());
    }

    @Test
    public void mergeAndEmptyCommit() throws Exception {
        final String commitMessage = "NEW MESSAGE ADDED IN TEST COMMIT";

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        Branch deploymentBranch = git.getBranch(session, correctDeploymentBranchLocalName());
        git.mergeWithNoFastForward(session, deploymentBranch, correctMasterBranchLocalName());
        git.commit(session, commitMessage, correctAuthor(), correctAuthorEmail());

        assertThat(currentFirstLineInFileHead(), is(equalTo(correctMasterRefHead())));
        assertThat(currentFirstLineInFileCommitEditMsg(), is(equalTo(commitMessage)));
        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
        assertThat(currentLinesInFileFetchHeadFilteredByMaster(), not(startsWith(currentFirstLineInFileRefsHeadsMaster())));
    }

    @Test
    public void hasDifferences_WhenDifferent() throws Exception {
        assertTrue(git.hasDifferences(session, correctMasterBranchLocalName(), correctDeploymentBranchLocalName()));
    }

    @Test
    public void hasDifferences_WhenEquals() throws Exception {
        assertFalse(git.hasDifferences(session, correctMasterBranchLocalName(), correctMasterBranchLocalName()));
    }

    @Test(expected = GitServiceException.class)
    public void hasDifferences_WhenInvalidBranch() throws Exception {
        assertFalse(git.hasDifferences(session, correctMasterBranchLocalName(), invalidBranchLocalName()));
    }


}