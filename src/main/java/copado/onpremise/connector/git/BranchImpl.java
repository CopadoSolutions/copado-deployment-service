package copado.onpremise.connector.git;

import lombok.Data;
import org.eclipse.jgit.lib.ObjectId;

@Data
class BranchImpl implements Branch {

    private String name;
    private ObjectId id;

    public String getIdentifier(){
        return id.getName();
    }

}
