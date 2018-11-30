package copado.onpremise.service.credential;


import com.google.inject.AbstractModule;

public class CredentialModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SalesforceCredentialService.class).to(SalesforceCredentialServiceImpl.class);
    }
}