package copado.onpremise.service.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.nio.file.Path;

interface GitServiceRemote {

    Git cloneRepository(String url, UsernamePasswordCredentialsProvider credentialsProvider, Path baseDirGit) throws GitAPIException;

}
