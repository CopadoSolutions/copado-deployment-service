package copado.validator.onpremisedeployment;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;

@Data
@AllArgsConstructor
class FileWithHash {
    private String path;
    private String hash;
}
