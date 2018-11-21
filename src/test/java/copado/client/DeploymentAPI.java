package copado.client;

import copado.onpremise.controller.*;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface DeploymentAPI {

    @POST("copado/onpremise/v1/deployment/deploy")
    Call<String> getDeploy(@Body DeployRequest deployRequest);

}
