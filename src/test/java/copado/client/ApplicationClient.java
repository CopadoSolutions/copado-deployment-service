package copado.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import copado.onpremise.Application;
import copado.onpremise.controller.DeployRequest;
import copado.onpremise.security.TokenGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.junit4.SpringRunner;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


@RunWith(SpringRunner.class)
@ComponentScan(basePackages = {"copado.onpremise"})
@SpringBootTest(classes = Application.class)
public class ApplicationClient implements Callback<String> {

    @Autowired
    private TokenGenerator tokenGenerator;

    @Test
    public void doTest() {

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

        //Callback-object
        ApplicationClient client = new ApplicationClient();

        //Do request
        DeployRequest request = new DeployRequest();
        // TODO: Edit here with your custom test values
        request.setDeploymentJobId("a0C1n00001EDt6N"); // Check a deployment-step (from there you can reach the deployment-job-id)
        request.setPromoteBranch("promote_branch");
        request.setTargetBranch("target_branch");
        request.setDeploymentBranch("deployment_branch");
        request.setCopadoJobId("TEST_COPADO_JOB_ID");
        request.setOrgDestId("ORG_ID_DEST");


        DeploymentAPI api = retrofit.create(DeploymentAPI.class);
        Call<String> call = api.getDeploy(request);
        call.enqueue(client);
    }


    @Override
    public void onResponse(Call<String> call, Response<String> response) {
        if (response.isSuccessful()) {
            String body = response.body();
            System.out.println("RECEIVED => " + body);
        } else {
            System.out.println(response.toString());
        }
    }

    @Override
    public void onFailure(Call<String> call, Throwable t) {
        t.printStackTrace();
    }

}
