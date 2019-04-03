package copado.onpremise;

import com.google.inject.AbstractModule;
import copado.onpremise.service.salesforce.*;

import static org.mockito.Mockito.mock;

public class SalesforceModuleMock  extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoService.class).toInstance(mock(CopadoService.class));
        bind(MetadataConnectionService.class).toInstance(mock(MetadataConnectionService.class));
        bind(SalesforceService.class).toInstance(mock(SalesforceService.class));
        bind(PartnerConnectionBuilder.class).toInstance(mock(PartnerConnectionBuilder.class));
    }

}