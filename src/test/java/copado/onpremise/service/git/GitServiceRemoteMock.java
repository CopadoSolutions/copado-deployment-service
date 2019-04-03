package copado.onpremise.service.git;

import lombok.extern.flogger.Flogger;

@Flogger
public class GitServiceRemoteMock {

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
