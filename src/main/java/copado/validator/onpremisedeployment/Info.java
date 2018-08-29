package copado.validator.onpremisedeployment;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;

@Data
@AllArgsConstructor
public class Info {
    private Path zipFile;
    private Path promotionPath;
}
