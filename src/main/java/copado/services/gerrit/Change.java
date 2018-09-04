package copado.services.gerrit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
class Change {

    private String id;
    private String project;
    @JsonProperty("change_id")
    private String changeId;
    private Label labels = new Label();
}