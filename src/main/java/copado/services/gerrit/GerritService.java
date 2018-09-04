package copado.services.gerrit;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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

    private Retrofit connection;
    private GerritAPI api;

    @PostConstruct
    public void init(){

        OkHttpClient.Builder builder = new OkHttpClient().newBuilder().addInterceptor(new GerritBasicAuthInterceptor());

        //Set lenient to the builder
        Gson gson = new GsonBuilder().setLenient().create();

        //Create connection
        connection = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).baseUrl(SystemProperties.GERRIT_ENDPOINT.value()).client(builder.build()).build();

        api = connection.create(GerritAPI.class);
    }

    public boolean isValidChange(String changeId) {

        log.info("Checking if chage-id:'{}' is valid.",changeId);
        Call<Change> call = api.getChangeDetail(changeId);
        try {
            Response<Change> response = call.execute();

            if(!response.isSuccessful()){
                throw new Exception("Response is not successful.");
            }

            Change change = response.body();
            log.info("Change '{}' verified:'{}'",changeId,change.getLabels().getCodeReviewVerfied());
            return change.getLabels().getCodeReviewVerfied();

        } catch (Exception e) {
            log.error("Could not retrieve Gerrit change:'{}'. Error:",changeId,e);
        }
        return false;
    }

}
