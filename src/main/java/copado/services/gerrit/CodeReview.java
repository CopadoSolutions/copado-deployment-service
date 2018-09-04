package copado.services.gerrit;

import lombok.Data;

import java.util.Map;

@Data
class CodeReview {
    private Map<String,String> values;
}
