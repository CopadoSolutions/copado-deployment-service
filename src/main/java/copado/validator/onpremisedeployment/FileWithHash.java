package copado.validator.onpremisedeployment;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
class FileWithHash {
    private String path;
    private String hash;
}
