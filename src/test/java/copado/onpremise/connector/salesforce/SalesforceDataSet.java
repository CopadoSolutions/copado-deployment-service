package copado.onpremise.connector.salesforce;

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
