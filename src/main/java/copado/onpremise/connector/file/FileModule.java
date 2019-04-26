package copado.onpremise.connector.file;


import com.google.inject.AbstractModule;

public class FileModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(PathService.class).to(PathServiceImpl.class);
    }
}