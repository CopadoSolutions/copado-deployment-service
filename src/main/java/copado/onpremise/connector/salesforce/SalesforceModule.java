package copado.onpremise.connector.salesforce;


import com.google.inject.AbstractModule;
import com.google.inject.Provides;

public class SalesforceModule extends AbstractModule {

    private SalesforceUtils salesforceUtils = new SalesforceUtils();

    @Override
    protected void configure() {
        bind(PartnerConnectionBuilder.class).to(PartnerConnectionBuilderImpl.class);
    }

    @Provides
    SalesforceUtils salesforceUtils() {
        return salesforceUtils;
    }

}