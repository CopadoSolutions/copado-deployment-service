package copado.onpremise.connector.salesforce;

import copado.onpremise.job.DeployRequest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SalesforceDataSource {
    private byte[] correctZipBytes;
    private DeployRequest deployRequest;
}
