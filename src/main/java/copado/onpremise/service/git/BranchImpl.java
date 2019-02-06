package copado.onpremise.service.git;

import lombok.Data;
import org.eclipse.jgit.lib.ObjectId;

@Data
class BranchImpl implements Branch {

    private String name;
    private ObjectId id;

}
