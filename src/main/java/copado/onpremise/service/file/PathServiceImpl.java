package copado.onpremise.service.file;

import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;

@Slf4j
@AllArgsConstructor(onConstructor = @__({ @Inject}))
public class PathServiceImpl implements PathService {

    /**
     * Removes the directory if exists.
     *
     * @param dir
     */
    public void safeDelete(Path dir) {
        log.info("Safe deleting dir:{}", dir);
        if (dir != null && dir.toFile().isDirectory()) {
            try {
                FileUtils.deleteDirectory(dir.toFile());
            } catch (Exception e) {
                log.error("Could not delete directory:'{}', exception:'{}'", dir, e);
            }
        }
        log.info("FINISHED deleting dir:{}", dir);

    }
}
