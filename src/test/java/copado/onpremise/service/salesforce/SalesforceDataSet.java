package copado.onpremise.service.salesforce;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SalesforceDataSet {
    private DeploymentResult deploymentResult;
    private String attachmentId;
}
