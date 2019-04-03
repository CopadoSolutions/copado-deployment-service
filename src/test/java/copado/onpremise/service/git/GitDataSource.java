package copado.onpremise.service.git;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.io.IOException;
import java.nio.file.Path;

@Data
@Builder
@AllArgsConstructor
public class GitDataSource {

    private Path currentBaseGitDir;
    private String currentTestFolder = "gitService";

    public GitDataSource() {
        currentBaseGitDir = GitTestFactory.createGitTempDir();
    }
}
