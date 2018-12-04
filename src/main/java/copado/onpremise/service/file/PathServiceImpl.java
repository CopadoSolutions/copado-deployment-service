package copado.onpremise.service.file;

import com.google.common.flogger.FluentLogger;
import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;


@AllArgsConstructor(onConstructor = @__({ @Inject}))
public class PathServiceImpl implements PathService {

    private static final FluentLogger log = FluentLogger.forEnclosingClass();

    /**
     * Removes the directory if exists.
     *
     * @param dir
     */
    public void safeDelete(Path dir) {
        log.atInfo().log("Safe deleting dir:%s", dir);
        if (dir != null && dir.toFile().isDirectory()) {
            try {
                FileUtils.deleteDirectory(dir.toFile());
            } catch (Exception e) {
                log.atSevere().log("Could not delete directory:'%s', exception:'%s'", dir, e);
            }
        }
        log.atInfo().log("FINISHED deleting dir:%s", dir);

    }
}
