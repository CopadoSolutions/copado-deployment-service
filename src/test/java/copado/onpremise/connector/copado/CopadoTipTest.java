package copado.onpremise.connector.copado;

import copado.onpremise.connector.salesforce.TipLevel;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Test;

import java.io.IOException;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class CopadoTipTest {

    private ObjectMapper objectMapper = new ObjectMapper();
    private final String message = "This is a test error";
    private final String tip = "Sorry, no tips. It is a test";
    private final TipLevel errorLevel = TipLevel.ERROR;

    @Test
    public void readValueFromString_withCorrectJson() throws IOException {

        final CopadoTip expectedTip = new CopadoTip(errorLevel, message, tip);

        CopadoTip currentTip = objectMapper.readValue(correctJson(), CopadoTip.class);

        assertThat(currentTip, is(equalTo(expectedTip)));
    }

    @Test
    public void writeValueToSTring_withObject() throws IOException {

        final CopadoTip currentTip = new CopadoTip(errorLevel, message, tip);

        String currentJson = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(currentTip);

        assertThat(currentJson, is(equalTo(correctJson())));
    }

    private String correctJson(){

        return "{\n" +
                "  \"l\" : \"" + errorLevel + "\",\n" +
                "  \"m\" : \"" + message + "\",\n" +
                "  \"t\" : \"" + tip + "\"\n" +
                "}";
    }

}