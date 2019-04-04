package copado.onpremise.connector.git;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class GitModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(GitService.class).to(GitServiceImpl.class);
        bind(GitServiceRemote.class).to(GitServiceRemoteImpl.class);
    }

    @Provides
    GitSession gitSessionProvider(){
        return new GitSessionImpl();
    }

    @Provides
    Branch branchProvider(){
        return new BranchImpl();
    }

}