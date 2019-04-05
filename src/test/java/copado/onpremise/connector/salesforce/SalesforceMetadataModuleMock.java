package copado.onpremise.connector.salesforce;

import com.google.inject.AbstractModule;
import copado.onpremise.connector.salesforce.metadata.MetadataConnectionService;

import static org.mockito.Mockito.mock;

public class SalesforceMetadataModuleMock extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetadataConnectionService.class).toInstance(mock(MetadataConnectionService.class));
    }

}