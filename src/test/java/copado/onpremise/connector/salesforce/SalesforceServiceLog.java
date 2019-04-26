package copado.onpremise.connector.salesforce;

import copado.onpremise.job.DeployRequest;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class SalesforceServiceLog {

    List<DeployRequest> deployRequests = new ArrayList<>();
    List<byte[]> zipsBytes = new ArrayList<>();
}
