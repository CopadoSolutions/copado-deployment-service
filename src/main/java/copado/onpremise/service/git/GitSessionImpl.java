package copado.onpremise.service.git;

import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
@Scope("prototype")
@Data
class GitSessionImpl implements GitSession {

    private Git git;
    private Path baseDir;

}
