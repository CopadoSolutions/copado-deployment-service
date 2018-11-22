package copado.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import copado.onpremise.ApplicationConfiguration;
import copado.onpremise.controller.DeployRequest;
import copado.onpremise.security.TokenGenerator;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = {ApplicationConfiguration.class, TokenGenerator.class})
public class ApplicationClient {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Test
    public void doTest() throws IOException {

        //Create token
        String token = tokenGenerator.generateToken().get();
        System.out.println("Generated token : " + token);

        //Set headers for all requests
        OkHttpClient.Builder builder = new OkHttpClient().newBuilder();
        builder.addInterceptor(chain -> {
            Request request = chain.request().newBuilder().addHeader("token", token).build();
            return chain.proceed(request);
        });

        //Set lenient to the builder
        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        //Create connection
        Retrofit retrofit = new Retrofit.Builder().addConverterFactory(GsonConverterFactory.create(gson)).baseUrl("http://localhost:8080/").client(builder.build()).build();

        //Do request
        DeployRequest request = new DeployRequest();
        // TODO: Edit here with your custom test values
        request.setDeploymentJobId("a0C1n00001EDt6N"); // Check a deployment-step (from there you can reach the deployment-job-id)
        request.setPromoteBranch("promote_branch");
        request.setTargetBranch("target_branch");
        request.setDeploymentBranch("deployment_branch");
        request.setCopadoJobId("TEST_COPADO_JOB_ID");
        request.setOrgDestId("00D1t000000otHB"); // Your destination org where the zip will be deployed


        DeploymentAPI api = retrofit.create(DeploymentAPI.class);
        Call<String> call = api.getDeploy(request);
        log.info("RECEIVED => " + call.execute().body());
        log.info("Test finished!");
       // call.enqueue(client);
    }

}
