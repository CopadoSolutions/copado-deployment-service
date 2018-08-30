package copado.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import javax.validation.constraints.NotNull;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@Slf4j
public class PathUtils {

    /**
     * Returns a string with the relative path for a file within a directory.<br/>
     * e.g: <br/>
     * <li>parentDir: /tmp</li>
     * <li>filePath: /tmp/file1.txt</li>
     * <li>returns: file1.txt</li>
     *
     * @param parentDir must be a directory
     * @param filePath must be a regular file
     * @return relative path for filePath within parentDir directory.
     */
    public static Optional<String> getRelativePath(@NotNull Path parentDir, @NotNull Path filePath){

        if(Files.isDirectory(parentDir) && Files.isRegularFile(filePath)) {

            String parentStr = parentDir.toAbsolutePath().toString();
            String actualPathStr = filePath.toAbsolutePath().toString();

            try{
                Path relativePath = parentDir.relativize(filePath);
                if(relativePath != null && !"".equals(relativePath.toString())){
                    return Optional.of(relativePath.toString());
                }
            }catch (Exception e){
                log.error("Could not relativize paths:'{}', and:'{}'",parentDir,filePath);
            }
        }

        return Optional.empty();
    }

    /**
     * Removes the directory if exists.
     *
     * @param dir
     */
    public static void safeDelete(Path dir) {
        log.info("Safe deleting dir:{}", dir);
        if (dir != null && Files.isDirectory(dir)) {
            try {
                FileUtils.deleteDirectory(new File(dir.toAbsolutePath().toString()));
            } catch (Exception e) {
                log.error("Could not delete directory:'{}', exception:'{}'", dir, e);
            }
        }
        log.info("FINISHED deleting dir:{}", dir);

    }
}
