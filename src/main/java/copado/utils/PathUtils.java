package copado.utils;

import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.regex.Pattern;

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
}
