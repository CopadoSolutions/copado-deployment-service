package copado.onpremise.service.salesforce;

import com.google.inject.AbstractModule;

import static org.mockito.Mockito.mock;

public class SalesforceModuleMock extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoService.class).to(CopadoServiceImpl.class);
        bind(MetadataConnectionService.class).toInstance(mock(MetadataConnectionService.class));
        bind(SalesforceService.class).to(SalesforceServiceMock.class);
        bind(PartnerConnectionBuilder.class).toInstance(mock(PartnerConnectionBuilder.class));
    }

}