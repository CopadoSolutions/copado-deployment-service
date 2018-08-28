package copado.client;

import retrofit2.Call;
import retrofit2.http.GET;

public interface DeploymentAPI {

    @GET("copado/v1/on-premise-deployment/deploy")
    Call<String> getDeploy();
}
