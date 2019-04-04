package copado.onpremise.service.salesforce;

import com.google.inject.AbstractModule;

import static org.mockito.Mockito.mock;

public class SalesforceModuleAsserts extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoService.class).to(CopadoServiceImpl.class);
        bind(MetadataConnectionService.class).toInstance(mock(MetadataConnectionService.class));
        bind(SalesforceService.class).to(SalesforceServiceAssert.class);
        bind(PartnerConnectionBuilder.class).toInstance(mock(PartnerConnectionBuilder.class));
    }

}