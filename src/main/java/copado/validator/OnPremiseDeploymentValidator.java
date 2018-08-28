package copado.validator;

import ch.qos.logback.core.util.FileUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.FileUtils;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class OnPremiseDeploymentValidator implements Validator<OnPremiseDeploymentValidator.Info> {

    @Data
    public class Info {
        private Path zipFile;
        private Path promotionPath;
    }

    private static final String TEMP = "/temp";

    @Override
    public boolean validate(Info info) {
        boolean isValid = false;
        try {
            if (!info.zipFile.endsWith(".zip")) {
                throw new ValidatorException(String.format("Wrong extension for zip file:'%s'", info.zipFile.toAbsolutePath()));
            }

            Optional<Path> zipDecompressedPath = decompressZip(info.zipFile);
            if(zipDecompressedPath.isPresent()){
                Map<String, String> zipSHA256 = buildSHA256ForZip(zipDecompressedPath.get());
                isValid = checkSHA256ForPromotion(zipSHA256, info.getPromotionPath());
            }

        } catch (Exception e) {
            log.error("Could not validate info:{}, exception:{}", info, e);
            return isValid;
        }
        return isValid;
    }




    private Optional<Path> decompressZip(Path zipFile) {
        Path tmpDir = null;

        try {
            tmpDir = Files.createTempDirectory(TEMP);
            decompressZip(zipFile,tmpDir);
        } catch (Exception e) {
            log.error("Could not create temporal directory on:'{}', exception:'{}'", TEMP,e);
            safeDelete(tmpDir);
        }

        return Optional.empty();
    }

    private void decompressZip(Path zipFile, Path pathForDecompress) throws Exception {

        log.info("Starting to decompress zip file:'', into path:'{}'",zipFile,pathForDecompress);

        byte[] buffer = new byte[1024];

        //get the zip file content
        try(ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFile.toAbsolutePath().toString()))) {

            //get the zipped file list entry
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) {

                String fileName = ze.getName();
                File newFile = new File(pathForDecompress.toAbsolutePath().toString() + File.separator + fileName);

                log.info("Unzipping file:'{}'", newFile.getAbsoluteFile());

                //create all non exists folders
                //else you will hit FileNotFoundException for compressed folder
                new File(newFile.getParent()).mkdirs();

                FileOutputStream fos = new FileOutputStream(newFile);

                int len;
                while ((len = zis.read(buffer)) > 0) {
                    fos.write(buffer, 0, len);
                }

                fos.close();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
        }

        log.info("FINISHED! Decompressed zip file:'', into path:'{}'",zipFile,pathForDecompress);
    }

    private void safeDelete(Path dir){
        log.info("Safe deleting dir:{}", dir);
        if(dir != null){
            try {
                Files.delete(dir);
            } catch (Exception e) {
                log.error("Could not delete directory:'{}', exception:'{}'", TEMP,e);
            }
        }
    }

    private Map<String, String> buildSHA256ForZip(Path path) {
        //TODO Implementar
    }

    private boolean checkSHA256ForPromotion(Map<String, String> zipSHA256, Path promotionPath) {
        //TODO Implementar

    }

}
