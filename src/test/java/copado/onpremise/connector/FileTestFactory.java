package copado.onpremise.connector;

import org.apache.commons.io.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileTestFactory {

    public static byte[] bytesOf(String zipFileAbsolutePath) {
        return bytesOf(Paths.get(zipFileAbsolutePath));
    }

    public static byte[] bytesOf(Path zipFileAbsolutePath) {
        try {
            return FileUtils.readFileToByteArray(zipFileAbsolutePath.toFile());
        } catch (IOException e) {
            throw new RuntimeException("Could not read zip bytes: " + zipFileAbsolutePath, e);
        }
    }


}
