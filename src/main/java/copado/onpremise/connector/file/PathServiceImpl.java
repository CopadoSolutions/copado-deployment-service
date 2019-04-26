package copado.onpremise.connector.file;


import com.google.inject.Inject;
import lombok.AllArgsConstructor;
import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;

import java.nio.file.Path;


@AllArgsConstructor(onConstructor = @__({ @Inject}))
@Flogger
public class PathServiceImpl implements PathService {

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
