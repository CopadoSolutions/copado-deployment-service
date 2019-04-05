package copado.onpremise.connector.salesforce.metadata;

import copado.onpremise.connector.copado.CopadoTip;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentResult {
    private List<CopadoTip> tips = new ArrayList<>();
    private boolean success;
    private Path deployedZipPath;
    private String asyncId;
}
