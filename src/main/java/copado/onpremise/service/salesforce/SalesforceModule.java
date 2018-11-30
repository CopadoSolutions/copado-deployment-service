package copado.onpremise.service.salesforce;


import com.google.inject.AbstractModule;

public class SalesforceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoService.class).to(CopadoServiceImpl.class);
        bind(MetadataConnectionService.class).to(MetadataConnectionServiceImpl.class);
        bind(SalesforceService.class).to(SalesforceServiceImpl.class);
    }

}