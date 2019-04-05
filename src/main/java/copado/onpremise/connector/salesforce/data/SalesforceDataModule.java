package copado.onpremise.connector.salesforce.data;


import com.google.inject.AbstractModule;

public class SalesforceDataModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(SalesforceService.class).to(SalesforceServiceImpl.class);
    }

}