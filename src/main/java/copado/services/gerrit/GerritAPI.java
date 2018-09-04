package copado.services.gerrit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

interface GerritAPI {

    @GET("a/change/{changeId}/detail")
    Call<Change> getChangeDetail(@Path("changeId") String changeId);
}
