package copado.services.gerrit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;

@Data
class Label {

    private Boolean codeReviewVerfied = false;

    @JsonProperty("Code-Review")
    private void unpackCodeReview(Map<String,Object> verified) {
       Map<String,String> values = (Map<String,String>) verified.get("values");
       if(values != null){
           String valuePlus2 = values.get("+2");
           if(valuePlus2 != null){
               codeReviewVerfied = true;
           }
       }
    }
}
