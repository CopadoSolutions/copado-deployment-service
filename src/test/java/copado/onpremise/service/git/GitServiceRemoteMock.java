package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;
import java.nio.file.Paths;

@Flogger
public class GitServiceRemoteMock implements GitServiceRemote {

    @Override
    public Git cloneRepository(String repositoryName, UsernamePasswordCredentialsProvider credentialsProvider, Path temporalDir) throws GitAPIException {

        Path originalRepository = Paths.get("src", "test", "resources", "repositories", repositoryName);
        checkWithException(originalRepository.toFile().exists(), "Could not find original repository: ", originalRepository);
        handleExceptions(() -> {
            FileUtils.copyDirectory(originalRepository.toFile(), temporalDir.toFile());

            return Void.TYPE;
        });

        return Git.init().setDirectory(temporalDir.toFile()).call();
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
