package copado.service.git;

import lombok.Data;
import org.eclipse.jgit.lib.ObjectId;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Data
@Component
@Scope("prototype")
class BranchImpl implements Branch {

    private String name;
    private ObjectId id;

}
