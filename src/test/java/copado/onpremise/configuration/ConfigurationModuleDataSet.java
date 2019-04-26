package copado.onpremise.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.configuration.CompositeConfiguration;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationModuleDataSet {
    private CompositeConfiguration compositeConfiguration;
    private String deploymentOrgId;
    private ApplicationConfiguration applicationConfiguration;
}
