package copado.onpremise.security;

import copado.onpremise.ApplicationConfiguration;
import copado.onpremise.service.crypto.CryptoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Calendar;
import java.util.Optional;
import java.util.TimeZone;

/**
 * Generator of token based on UTC timestamp and SHA-256
 */
@Slf4j
@Component
public class TokenGenerator {

    @Autowired
    private ApplicationConfiguration config;

    /**
     * Looks for <code>ENDPOINT_CRYPTO_KEY</code> enviroment variable and build a token with "hour" based timestamp
     *
     * @return generated token or empty if could not create the token.
     */
    public Optional<String> generateToken() {


        Calendar now = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        String year = String.valueOf(now.get(Calendar.YEAR));
        String month = String.valueOf(now.get(Calendar.MONTH));
        String day = String.valueOf(now.get(Calendar.DAY_OF_MONTH));
        String hour = String.valueOf(now.get(Calendar.HOUR_OF_DAY));

        String timeStamp = year + month + day + hour;

        try {
            String tokenWithTimeStamp = config.getSecret() + "_" + timeStamp;
            return CryptoService.buildSHA256(tokenWithTimeStamp);
        } catch (Exception e) {
            log.error("An error occurs while generating token. Error: {}", e);
            return Optional.empty();
        }
    }
}
