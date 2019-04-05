package copado.onpremise.connector.copado;

import copado.onpremise.connector.salesforce.TipLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.codehaus.jackson.annotate.JsonProperty;

@Data
@AllArgsConstructor
public class CopadoTip {

    @JsonProperty("l")
    private TipLevel errorLevel;

    @JsonProperty("m")
    private String message;

    @JsonProperty("t")
    private String tip;
}
