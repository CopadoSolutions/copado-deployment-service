package copado.onpremise.service.git;

import lombok.Data;
import org.eclipse.jgit.lib.ObjectId;

@Data
public class Branch {

    private final String name;
    private final ObjectId id;

}
