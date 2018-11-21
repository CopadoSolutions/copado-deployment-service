package copado.client;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import copado.security.TokenGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


public class ApplicationClient implements Callback<String> {

    @Autowired
    private TokenGenerator tokenGenerator;

    //TODO: Spring-run
    public void main(String[] args) {

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
        DeploymentAPI api = retrofit.create(DeploymentAPI.class);
        Call<String> call = api.getDeploy("a0C0Y00000XTLeAUAX", "promote_branch", "target_branch", "deployment_branch", "DMD_Test~master~I294fb4a0a5cea1cb55026d21e6045140b230acfa");
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
