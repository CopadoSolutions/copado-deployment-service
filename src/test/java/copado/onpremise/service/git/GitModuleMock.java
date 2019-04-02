package copado.onpremise.service.git;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class GitModuleMock extends AbstractModule {

    @Override
    protected void configure() {

        bind(GitService.class).to(GitServiceImplMock.class);
        bind(GitServiceRemote.class).to(GitServiceRemoteMock.class);
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