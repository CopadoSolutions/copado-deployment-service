package copado.onpremise.service.credential;


interface PropertyCredentialsProvider {

    String getCredentialField(String orgId, String prefix, String field);

    String buildProperty(String orgId, String prefix, String propertyName);

}
