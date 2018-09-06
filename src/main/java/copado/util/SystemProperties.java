package copado.util;


public enum SystemProperties {

    COPADO_USERNAME("COPADO_USERNAME"),
    COPADO_URL("COPADO_URL"),
    COPADO_PASSWORD("COPADO_PASSWORD"),
    COPADO_TOKEN("COPADO_TOKEN"),
    GIT_URL("GIT_URL"),
    GIT_USERNAME("GIT_USERNAME"),
    GIT_PASSWORD("GIT_PASSWORD"),
    ORGID_USERNAME("ORGID_USERNAME"),
    ORGID_PASSWORD("ORGID_PASSWORD"),
    ORGID_URL("ORGID_URL"),
    ORGID_TOKEN("ORGID_TOKEN"),
    ENDPOINT_CRYPTO_KEY("ENDPOINT_CRYPTO_KEY"),
    GERRIT_ENDPOINT("GERRIT_ENDPOINT"),
    GERRIT_USER("GERRIT_USER"),
    GERRIT_PASSWORD("GERRIT_PASSWORD"),
    PROXY_HOST("PROXY_HOST"),
    PROXY_PASSWORD("PROXY_PASSWORD"),
    PROXY_PORT("PROXY_PORT"),
    PROXY_USERNAME("PROXY_USERNAME"),
    RENAME_NAMESPACE("RENAME_NAMESPACE")
    ;



    private String value;

    SystemProperties(String value) {
        this.value = value;
    }

    /**
     * Returns system.env value for variable
     *
     * @return
     */
    public String value() {
        return System.getenv(this.value);
    }

}
