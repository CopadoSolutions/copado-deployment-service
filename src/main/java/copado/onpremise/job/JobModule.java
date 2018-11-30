package copado.onpremise.job;


import com.google.inject.AbstractModule;

public class JobModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Job.class).to(OnPremiseDeploymentJob.class);
    }
}