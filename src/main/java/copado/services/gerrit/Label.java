package copado.services.gerrit;

import com.google.gson.annotations.SerializedName;
import lombok.Data;

@Data
class Label {
    @SerializedName("Code-Review")
    private CodeReview codeReview;
}
