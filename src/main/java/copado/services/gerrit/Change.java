package copado.services.gerrit;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
class Change {

    private String id;
    private String project;
    @SerializedName("change_id")
    private String changeId;
    private Label labels;
}