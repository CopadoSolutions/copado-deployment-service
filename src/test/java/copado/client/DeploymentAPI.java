package copado.client;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface DeploymentAPI {

    @GET("copado/v1/on-premise-deployment/deploy")
    Call<String> getDeploy( @Query("promoteBranch") String promoteBranch,
                            @Query("targetBranch") String targetBranch,
                            @Query("deploymentBranch") String deploymentBranch,
                            @Query("gerritChangeId") String gerritChangeId
    );
}
