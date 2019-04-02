package copado.onpremise.service.git;

import com.google.inject.Provider;

public class GitServiceImplMock extends GitServiceImpl{

    public GitServiceImplMock(Provider<GitSession> gitSessionProvider, Provider<Branch> gitBranchProvider, GitServiceRemote gitServiceRemote) {
        super(gitSessionProvider, gitBranchProvider, gitServiceRemote);
    }


}
