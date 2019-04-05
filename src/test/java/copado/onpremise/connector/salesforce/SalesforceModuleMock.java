package copado.onpremise.connector.salesforce;

import com.google.inject.AbstractModule;

import static org.mockito.Mockito.mock;

public class SalesforceModuleMock extends AbstractModule {

    @Override
    protected void configure() {
        bind(PartnerConnectionBuilder.class).toInstance(mock(PartnerConnectionBuilder.class));
    }

}