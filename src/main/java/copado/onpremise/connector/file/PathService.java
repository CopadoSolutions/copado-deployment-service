package copado.onpremise.connector.file;


import java.nio.file.Path;



public interface PathService {

    /**
     * Removes the directory if exists.
     *
     * @param dir
     */
    void safeDelete(Path dir);
}
