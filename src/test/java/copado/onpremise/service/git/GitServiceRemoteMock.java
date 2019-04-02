package copado.onpremise.service.git;

import copado.onpremise.exception.CopadoException;
import copado.onpremise.exception.ExceptionHandler;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

@Flogger
public class GitServiceRemoteMock extends GitServiceRemoteImpl {

    @Override
    public Git cloneRepository(String repositoryName, UsernamePasswordCredentialsProvider credentialsProvider, Path temporalDir) throws CopadoException {

        Path originalRepository = Paths.get("src", "test", "resources", "repositories", repositoryName, "givenRemote", "test_repo.git");
        checkWithException(originalRepository.toFile().exists(), "Could not find original repository: ", originalRepository);
        return ExceptionHandler.rethrow(CopadoException.class, "Could not clone repository", () ->
            super.cloneRepository(originalRepository.toAbsolutePath().toString(), credentialsProvider, temporalDir)
        );

    }


    private void checkWithException(boolean createBaseDirGit, String message, Object obj) {
        if (!createBaseDirGit) {
            throw new RuntimeException(message + obj.toString());
        }
    }

    private <T> T handleExceptions(CodeBlock<T> codeBlock) {
        try {
            return codeBlock.execute();
        } catch (Throwable e) {
            log.atSevere().log("Internal error in git remote (Mock) service. Exception: " + e);
            throw new RuntimeException("Internal error in git remote (Mock) service", e);
        }
    }

    @FunctionalInterface
    interface CodeBlock<R> {
        R execute() throws Throwable; //NOSONAR
    }
}
