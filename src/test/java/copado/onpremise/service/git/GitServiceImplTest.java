package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static copado.onpremise.service.git.GitTestFactory.*;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@Flogger
public class GitServiceImplTest {

    private GitService git;
    private GitSession session;


    @Before
    public void setUp() throws IOException, GitServiceException {
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
    public void clone_repositoryWithCorrectFile() {
        assertTrue(correctFileInMaster().isFile());
        assertTrue(correctFileInMaster().exists());
    }

    @Test
    public void test_cloneBranchFromRepo() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());

        assertTrue(refsHeadOfDeploymentBranch().isFile());
        assertTrue(refsHeadOfDeploymentBranch().exists());
    }

    @Test
    public void test_checkout() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        git.checkout(session, correctDeploymentBranchLocalName());

        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
    }

    @Test
    public void test_getBranch() throws Exception {

        Branch givenBranch = git.getBranch(session, correctDeploymentBranchLocalName());

        assertThat(currentContentInFileFetchHead(), containsString(correctHeadInDeploymentBranch()));
        assertThat(currentContentInFileFetchHead(), containsString(correctDeploymentBranchLocalName()));
        assertThat(givenBranch.getIdentifier(), is(equalTo(correctHeadInDeploymentBranch())));
        assertThat(givenBranch.getName(), is(equalTo(correctDeploymentBranchRefsRemoteName())));

    }

    @Test
    public void test_merge() throws Exception {

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        Branch deploymentBranch = git.getBranch(session, correctDeploymentBranchLocalName());
        git.mergeWithNoFastForward(session, deploymentBranch, correctMasterBranchLocalName());

        assertThat(correctMasterRefHead(), is(equalTo(currentFirstLineInFileHead())));
        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
    }


    @Test
    public void test_commit_after_Merge() throws Exception {
        final String commitMessage = "NEW MESSAGE ADDED IN TEST COMMIT";

        git.cloneBranchFromRepo(session, correctDeploymentBranchLocalName());
        Branch deploymentBranch = git.getBranch(session, correctDeploymentBranchLocalName());
        git.mergeWithNoFastForward(session, deploymentBranch, correctMasterBranchLocalName());
        git.commit(session, commitMessage, correctAuthor(), correctAuthorEmail());

        assertThat(currentFirstLineInFileHead(), is(equalTo(correctMasterRefHead())));
        assertThat(currentLinesInFileCommitEditMsg(), is(equalTo(commitMessage)));
        assertTrue(correctFileInDeploymentBranch().isFile());
        assertTrue(correctFileInDeploymentBranch().exists());
        assertThat(currentLinesInFileFetchHeadFilteredByMaster(), not(startsWith(currentLinesInFileRefsHeadsMaster())));
    }



}