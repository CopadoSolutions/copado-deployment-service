package copado.utils;

import lombok.extern.slf4j.Slf4j;

import javax.validation.constraints.NotNull;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Optional;

@Slf4j
public class CryptoUtils {

    public static Optional<String> buildSHA256(@NotNull String str){
        return buildSHA256(str.getBytes());
    }

    public static Optional<String> buildSHA256(@NotNull byte[] bytes){
        try {
            log.info("Start building SHA256");

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(bytes);
            byte[] digest = messageDigest.digest();
            String encryptedString = DatatypeConverter.printHexBinary(digest);

            return Optional.of(encryptedString);
        }catch (Exception e){
            log.error("An error occurs while building SHA256. Error: {}",e);
            return Optional.empty();
        }
    }
}
