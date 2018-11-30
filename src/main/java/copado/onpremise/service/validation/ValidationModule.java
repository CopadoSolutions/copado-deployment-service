package copado.onpremise.service.validation;


import com.google.inject.AbstractModule;

public class ValidationModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(ValidationService.class).to(ValidationServiceImpl.class);
    }
}