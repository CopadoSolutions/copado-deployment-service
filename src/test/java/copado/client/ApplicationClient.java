package copado.client;



import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import copado.security.TokenGenerator;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApplicationClient implements Callback<String> {


    public static void main(String[] args) {

        //Create token
        String token = TokenGenerator.generateToken().get();
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
        Call<String> call = api.getDeploy();
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
