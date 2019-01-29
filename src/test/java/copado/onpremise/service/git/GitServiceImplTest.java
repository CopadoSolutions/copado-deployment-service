package copado.onpremise.service.git;

import copado.onpremise.configuration.ApplicationConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

@RunWith(PowerMockRunner.class)
@PrepareForTest(GitService.class)
public class GitServiceImplTest {

    private Path baseDirGit;

    @Before
    public void setUp() throws IOException {
        baseDirGit = Files.createTempDirectory("git").toAbsolutePath();
    }

    @After
    public void tearDown() throws IOException {
        FileUtils.deleteDirectory(baseDirGit.toFile());
    }


    @Test
    public void cloneRepo() throws  GitServiceException {

        ApplicationConfiguration configuration = ApplicationConfiguration.builder()
                .gitUrl("basic_repository")
                .gitUsername("mock_username")
                .gitPassword("mock_password")
                .build();

//         GitService gitService = new GitServiceImpl(configuration, GitSessionImpl::new, BranchImpl::new, new GitServiceRemoteMock());

   //      gitService.cloneRepo(baseDirGit);

    }


    @Test
    public void test_write_file_withexistingdirWiththesamename() throws IOException {
        Path tmpDir = Files.createTempDirectory("test");
        Path gitIgnoreDir = tmpDir.resolve(".gitignore");

        assert gitIgnoreDir.toFile().mkdirs();
        FileUtils.deleteDirectory(gitIgnoreDir.toFile());

        Files.write(gitIgnoreDir, Arrays.asList("Line 1", "Line 2"));

    }
}