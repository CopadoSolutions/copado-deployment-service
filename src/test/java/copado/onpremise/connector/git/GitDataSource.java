package copado.onpremise.connector.git;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.nio.file.Path;
import java.nio.file.Paths;

@Data
@Builder
@AllArgsConstructor
public class GitDataSource {

    private Path currentBaseGitDir;
    private Path currentRemoteDir;
    private Path currentFirstArtifactRemote;
    private Path currentSecondArtifactRemote;

    public GitDataSource(String currentTestFolder) {
        currentBaseGitDir = GitTestFactory.createGitTempDir();
        currentRemoteDir = Paths.get("src", "test", "resources", "repositories", currentTestFolder, "givenRemote", "test_repo.git").toAbsolutePath();
    }
}
