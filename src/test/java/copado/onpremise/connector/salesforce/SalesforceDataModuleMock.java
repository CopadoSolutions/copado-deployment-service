package copado.onpremise.connector.salesforce;

import com.google.inject.AbstractModule;
import copado.onpremise.connector.salesforce.data.SalesforceService;

public class SalesforceDataModuleMock extends AbstractModule {

    @Override
    protected void configure() {
        bind(SalesforceService.class).to(SalesforceServiceMock.class);
    }

}