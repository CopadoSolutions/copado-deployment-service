package copado.validator.onpremisedeployment;

import copado.exception.CopadoException;
import copado.util.CryptoUtils;
import copado.util.PathUtils;
import copado.validator.Validator;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@Slf4j
public class OnPremiseDeploymentValidator implements Validator<Info> {

    private static final String TEMP = "tempOnPremiseDeploymentValidator";
    private static final String ZIP_EXTENSION = "zip";

    @Override
    public boolean validate(Info info) {
        boolean isValid = false;
        try {
            log.info("Starting to validate info:{}", info);

            // Retrieve zip info and compare with promotion
            Optional<Path> zipDecompressedPath = decompressZip(info.getZipFile());
            if (zipDecompressedPath.isPresent()) {
                List<FileWithHash> zipSHA256list = buildSHA256ForZipPath(zipDecompressedPath.get());
                isValid = compareSHA256ForPath(zipSHA256list, info.getPromotionPath());
            }

        } catch (Exception e) {
            log.error("Could not validate info:{}, exception:{}", info, e);
        }

        log.info("FINISHED to validate info:{}, with value:{}", info, isValid);

        return isValid;
    }


    /**
     * Creates a temporal directory for decompress the zip, and if there is no error while decompressing
     * this temporal directory will be returned
     *
     * @param zipFile zip file
     * @return temporal directory with the zip content.
     */
    private static Optional<Path> decompressZip(Path zipFile) {
        Path tmpDir = null;

        try {
            log.info("Starting to decompress zip:'{}'", zipFile);
            // Check file extension
            String extension = FilenameUtils.getExtension(zipFile.toString());

            if (!ZIP_EXTENSION.equalsIgnoreCase(extension)) {
                throw new CopadoException(String.format("Wrong extension for zip file:'%s'", zipFile.toAbsolutePath()));
            }

            tmpDir = Files.createTempDirectory(TEMP);
            decompressZip(zipFile, tmpDir);

            log.info("FINISHED to decompress zip:'{}', temporal directory created:'{}'", zipFile, tmpDir);
            return Optional.of(tmpDir);

        } catch (CopadoException e) {
            log.error("Could not create temporal directory on:'{}', exception:'{}'", TEMP, e.getMessage());
            PathUtils.safeDelete(tmpDir);

        } catch (Exception e) {
            log.error("Could not create temporal directory on:'{}', exception:'{}'", TEMP, e);
            PathUtils.safeDelete(tmpDir);
        }

        return Optional.empty();
    }

    /**
     * Decompress given zipFile into the path for decompress
     *
     * @param zipFile
     * @param pathForDecompress
     * @throws Exception
     */
    private static void decompressZip(Path zipFile, Path pathForDecompress) throws IOException {

        log.info("Starting to decompress zip file:'{}', into path:'{}'", zipFile, pathForDecompress);

        byte[] buffer = new byte[1024];

        //get the zip file content
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toAbsolutePath().toString()))) {

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                if (!ze.isDirectory()) {

                    String fileName = ze.getName();
                    File newFile = new File(pathForDecompress.toAbsolutePath().toString() + File.separator + fileName);

                    log.info("Unzipping file:'{}'", newFile.getAbsoluteFile());

                    //create all non exists folders
                    //else you will hit FileNotFoundException for compressed folder
                    Path parentPath = newFile.getParentFile().toPath();
                    if (parentPath.toFile().exists()) {
                        parentPath.toFile().mkdirs();
                    }

                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }

                ze = zis.getNextEntry();
            }

            zis.closeEntry();
        }

        log.info("FINISHED! Decompressed zip file:'', into path:'{}'", zipFile, pathForDecompress);
    }


    /**
     * Given a path, it walk through all the regular files (in current directory and subdirectories)
     * and returns a <code>FileWithHash</code> list where:<br/>
     * <li><b>path: </b>relative file path to <code>path</code> parameter</li>
     * <li><b>hash: </b>Hash in SHA256 of the file.</li>
     *
     * @param path
     * @return
     */
    private static List<FileWithHash> buildSHA256ForZipPath(Path path) {

        log.info("Started building sha-256 hash list for path:'{}'", path);

        List<Path> filesInPath;
        try {

            try (Stream<Path> s = Files.walk(path)) {
                filesInPath = s.filter(p -> p.toFile().isFile()).collect(Collectors.toList());
            }

        } catch (IOException e) {
            log.error("Could not go over directory:'{}', exception:'{}'", path, e);
            return Collections.emptyList();
        }

        log.info("Regular file list correctly retrieved for path:'{}'", path);

        return filesInPath.stream()
                .map(file -> buildSHA256ForZipFile(path, file))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    /**
     * Returnsh a file hash builded with sha-256 for the given file.
     * The {@link FileWithHash}<code>.getPath()</code> is relative to <code>parentDir</code> directory.
     *
     * @param filePath
     * @return
     */
    private static Optional<FileWithHash> buildSHA256ForZipFile(Path parentDir, Path filePath) {

        log.debug("Started building sha-256 hash for file:'{}'", filePath);


        Optional<FileWithHash> fileWithHashOpt = buildSHA256ForFile(filePath);
        if (fileWithHashOpt.isPresent()) {
            Optional<String> relativePath = PathUtils.getRelativePath(parentDir, filePath);

            relativePath.ifPresent(s -> fileWithHashOpt.get().setPath(s));
        }

        log.debug("FINISHED building sha-256 hash for file:'{}'", filePath);

        return fileWithHashOpt;
    }

    /**
     * Returnsh a file hash builded with sha-256 for the given file.
     * The {@link FileWithHash}<code>.getPath()</code> is absolute.
     *
     * @param filePath
     * @return
     */
    private static Optional<FileWithHash> buildSHA256ForFile(Path filePath) {

        Optional<FileWithHash> fileWithHashOpt = Optional.empty();

        try {
            byte[] fileBytesContent = Files.readAllBytes(filePath);
            Optional<String> hashOpt = CryptoUtils.buildSHA256(fileBytesContent);
            if (hashOpt.isPresent()) {

                FileWithHash fwh = new FileWithHash(filePath.toAbsolutePath().toString(), hashOpt.get());
                fileWithHashOpt = Optional.of(fwh);

            }
        } catch (IOException e) {
            log.error("Could not build SHA256 for file:'{}', exception:'{}'", filePath, e);
        }

        return fileWithHashOpt;
    }

    /**
     * For each record in the given <code>fileWithHashList</code> (where the path must be relative), it check this file into given <code>dir</code> and generate a sha-256 hash for it.<br/>
     * Then both hashes are compared with each other.
     *
     * @param dir
     * @param fileWithHashList
     * @return <code>true</code>, when all files has the same hash in <code>fileWithHashList</code> than in <code>dir</code><br/>
     * <code>false</code>, when any file in <code>fileWithHashList</code> has a different hash in <code>dir</code>
     */
    private boolean compareSHA256ForPath(List<FileWithHash> fileWithHashList, Path dir) {

        log.info("Started comparing sha-256 hash with dir:'{}'", dir);


        boolean areDifferentFiles = false;
        for (int i = 0; !areDifferentFiles && i < fileWithHashList.size(); i++) {

            FileWithHash originalHashedFile = fileWithHashList.get(i);

            log.debug("originalHashedFile:{}", originalHashedFile);

            Optional<FileWithHash> actuallHashedFile = buildSHA256ForFile(dir.resolve(originalHashedFile.getPath()));

            //Check if has to stop
            // Stop when hash for files are different
            areDifferentFiles = actuallHashedFile.map(fileWithHash -> !fileWithHash.getHash().equals(originalHashedFile.getHash())).orElse(true);
        }

        boolean toReturn = !areDifferentFiles;
        log.info("FINISHED comparing sha-256 hash with dir:'{}', returns:'{}'", dir, toReturn);

        return toReturn;
    }
}
































