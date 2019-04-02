package copado.onpremise.service.git;

import copado.onpremise.exception.CopadoException;
import copado.onpremise.exception.ExceptionHandler;
import lombok.extern.flogger.Flogger;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;


@Flogger
class GitServiceRemoteImpl implements GitServiceRemote {

    @Override
    public Git cloneRepository(String url, UsernamePasswordCredentialsProvider credentialsProvider, Path baseDirGit) throws CopadoException {

        return ExceptionHandler.rethrow(CopadoException.class, "Could not clone repository", () ->
                Git.cloneRepository()
                        .setURI(url)
                        .setCredentialsProvider(credentialsProvider)
                        .setDirectory(baseDirGit.toAbsolutePath().toFile())
                        .call());

    }

    @Override
    public void push(GitSessionImpl gitSession, CredentialsProvider credentialsProvider) throws CopadoException {
        log.atInfo().log("Pushing to remote target branch");
        ExceptionHandler.rethrow(GitServiceException.class,
                "Could not push",
                () -> gitSession.getGit().push().setCredentialsProvider(credentialsProvider).call());
    }

}
