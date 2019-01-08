package copado.onpremise.service.git;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class GitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitService.class).to(GitServiceImpl.class);
    }
}