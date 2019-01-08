package copado.onpremise.service.git;

import lombok.Data;
import org.eclipse.jgit.api.Git;

import java.nio.file.Path;

@Data
public class GitSession {

    private final Git git;
    private final Path baseDir;

}
