package copado.service.git;

import lombok.Data;
import org.eclipse.jgit.api.Git;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@Data
class GitSessionImpl implements GitSession {

    private Git git;

}
