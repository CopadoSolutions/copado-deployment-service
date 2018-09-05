package copado.service.gerrit;

import copado.util.SystemProperties;
import okhttp3.Credentials;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

import java.io.IOException;

class GerritBasicAuthInterceptor implements Interceptor {

    private String credentials;

    public GerritBasicAuthInterceptor() {
        credentials = Credentials.basic(SystemProperties.GERRIT_USER.value(), SystemProperties.GERRIT_PASSWORD.value());
    }


    @Override
    public Response intercept(Chain chain) throws IOException {
        Request request = chain.request();
        Request authenticatedRequest = request.newBuilder().header("Authorization", credentials).build();
        return chain.proceed(authenticatedRequest);
    }
}