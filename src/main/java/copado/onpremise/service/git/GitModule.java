package copado.onpremise.service.git;


import com.google.inject.AbstractModule;

public class GitModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GitService.class).to(GitServiceImpl.class);
        bind(GitSession.class).to(GitSessionImpl.class);
        bind(Branch.class).to(BranchImpl.class);
    }

}