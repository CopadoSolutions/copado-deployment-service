package copado.onpremise.service.salesforce;

import lombok.Data;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Data
public class DeploymentResult {
    private List<CopadoTip> tips = new ArrayList<>();
    private boolean success;
    private Path deployedZipPath;
    private String asyncId;
}
