package copado.onpremise.connector.copadodx;


import com.google.inject.AbstractModule;

public class DxModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoDxService.class).to(CopadoDxServiceImpl.class);
    }

}