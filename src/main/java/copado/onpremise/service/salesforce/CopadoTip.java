package copado.onpremise.service.salesforce;

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
