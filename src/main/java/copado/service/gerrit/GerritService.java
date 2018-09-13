package copado.service.gerrit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import copado.exception.CopadoException;
import copado.util.SystemProperties;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import javax.annotation.PostConstruct;

@Service
@Slf4j
public class GerritService {

    private GerritAPI api;

    @PostConstruct
    public void init() {

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder().addInterceptor(new GerritBasicAuthInterceptor());

        //Set lenient to the builder
        Gson gson = new GsonBuilder().setLenient().create();

        //Create connection
        Retrofit connection = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).baseUrl(SystemProperties.GERRIT_ENDPOINT.value()).client(builder.build()).build();
        api = connection.create(GerritAPI.class);
    }

    public boolean isValidChange(String changeId) {

        log.info("Checking if chage-id:'{}' is valid.", changeId);
        Call<Change> call = api.getChangeDetail(changeId);
        try {
            Response<Change> response = call.execute();

            if (!response.isSuccessful()) {
                throw new CopadoException("Response is not successful.");
            }

            log.info("Response: ");


            Change change = response.body();
            if (change.getLabels() == null) {
                throw new CopadoException("Gerrit change does not have labels. Change id:" + changeId);
            }

            if (change.getLabels().getCodeReview() == null) {
                throw new CopadoException("Gerrit labels for change does not have Code-Review. Change id:" + changeId);
            }

            if (change.getLabels().getCodeReview().getValues() == null) {
                throw new CopadoException("Gerrit Code-Review for change label does not have values. Change id:" + changeId);
            }

            String verifiedStr = change.getLabels().getCodeReview().getValues().get("+2");

            log.info("Change '{}' verified:'{}'", changeId, verifiedStr);
            return verifiedStr != null;

        } catch (CopadoException e) {
            log.error("Could not retrieve Gerrit change:'{}'. Error:", changeId, e.getMessage());
        } catch (Exception e) {
            log.error("Could not retrieve Gerrit change:'{}'. Error:", changeId, e);
        }
        return false;
    }

}
