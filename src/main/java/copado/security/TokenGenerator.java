package copado.security;

import lombok.extern.slf4j.Slf4j;

import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Generator of token based on UTC timestamp and SHA-256
 */
@Slf4j
public class TokenGenerator {

    /**
     * Looks for <code>ENDPOINT_CRYPTO_KEY</code> enviroment variable and build a token with "hour" based timestamp
     * @return generated token or empty if could not create the token.
     */
    public static Optional<String> generateToken(){


        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String year = String.valueOf(now.get(Calendar.YEAR));
        String month = String.valueOf(now.get(Calendar.MONTH));
        String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));

        String timeStamp  = year + month + day + hour;

        try {
            String tokenWithTimeStamp = System.getenv("ENDPOINT_CRYPTO_KEY") + "_" + timeStamp;

            MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(tokenWithTimeStamp.getBytes());
            byte[] digest = messageDigest.digest();
            String encryptedString = DatatypeConverter.printHexBinary(digest);

            return Optional.of(encryptedString);
        }catch (Exception e){
            return Optional.empty();
        }
    }
}
