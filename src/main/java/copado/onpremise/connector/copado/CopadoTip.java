package copado.onpremise.connector.copado;

import copado.onpremise.connector.salesforce.TipLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CopadoTip {

    @JsonProperty("l")
    private TipLevel errorLevel;

    @JsonProperty("m")
    private String message;

    @JsonProperty("t")
    private String tip;
}
