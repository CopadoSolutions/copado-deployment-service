package copado.onpremise.service.git;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;

class GitServiceRemoteImpl implements GitServiceRemote {

    @Override
    public Git cloneRepository(String url, UsernamePasswordCredentialsProvider credentialsProvider, Path baseDirGit) throws GitAPIException {

        CloneCommand cloneCommand = Git.cloneRepository()
                .setURI(url)
                .setCredentialsProvider(credentialsProvider)
                .setDirectory(baseDirGit.toAbsolutePath().toFile());

        return cloneCommand.call();

    }

}
