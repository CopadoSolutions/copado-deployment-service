package copado.validator.onpremisedeployment;

import copado.validator.Validator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.Assert.*;

public class OnPremiseDeploymentValidatorTest {
    private ClassLoader classLoader;

    private Validator<Info> validator = new OnPremiseDeploymentValidator();
    private static final String BASE_DIR = "validator/on-premise-deployment-validator";
    private static final String PROMOTION_DIR = "promotion";
    private static final String ZIP_FILE_PATH = "files.zip";

    private Path baseDir;

    @Before
    public void beforeEachMethod(){
        classLoader = getClass().getClassLoader();
        baseDir = Paths.get(classLoader.getResource(BASE_DIR).getPath());
    }


    @Test
    public void test_validate_withRightZipAndPromotion_ShouldFinishOk() {
        baseDir = baseDir.resolve("ok");
        Path promotionDir = buildPromotionDir();
        Path zipPath = buildZipPath();

        assertTrue(validator.validate(new Info(zipPath,promotionDir)));
    }

    @Test
    public void test_validate_withDifferntFilesBetweenZipAndPromotionFolder_ShouldFinishKo() {
        baseDir = baseDir.resolve("error/different_file_in_zip");
        Path promotionDir = buildPromotionDir();
        Path zipPath = buildZipPath();

        assertFalse(validator.validate(new Info(zipPath,promotionDir)));
    }

    private Path buildPromotionDir(){
        return baseDir.resolve(PROMOTION_DIR);
    }
    private Path buildZipPath(){
        return baseDir.resolve(ZIP_FILE_PATH);
    }
}