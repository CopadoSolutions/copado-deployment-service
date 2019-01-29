package copado.onpremise.service.git;

import copado.onpremise.service.credential.GitCredentials;
import lombok.Data;
import org.eclipse.jgit.api.Git;

import java.nio.file.Path;

@Data
class GitSessionImpl implements GitSession {

    private Git git;
    private Path baseDir;
    private GitCredentials gitCredentials;

}
