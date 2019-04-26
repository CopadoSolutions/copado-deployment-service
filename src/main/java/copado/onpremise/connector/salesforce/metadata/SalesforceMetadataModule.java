package copado.onpremise.connector.salesforce.metadata;


import com.google.inject.AbstractModule;

public class SalesforceMetadataModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(MetadataConnectionService.class).to(MetadataConnectionServiceImpl.class);
    }

}