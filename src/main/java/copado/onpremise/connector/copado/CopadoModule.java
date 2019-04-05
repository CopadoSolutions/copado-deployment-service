package copado.onpremise.connector.copado;


import com.google.inject.AbstractModule;

public class CopadoModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(CopadoService.class).to(CopadoServiceImpl.class);
    }

}