# Project Title

This project runs Copado On-Premise Deployment Job, which allows to run your own code validations before deploy metadata to Salesforce.



## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. 

### Prerequisites

In order to be able to develop on this project, you will need to install:

* [Java jdk 1.8.0](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) or above

* Java development IDE

* [Maven](https://maven.apache.org/download.cgi)


### Installing

Follow this steps to install to get a development environment running

* Clone the repository

```
git clone https://github.com/CopadoSolutions/copado-deployment-service.git
```



* Import the project as Maven project to your Java development IDE.
* Build the project

```
mvn clean install
```

* Add the properties to the project, both possibilities availables:

  * As environment settings 
  * As application.properties or application.yml

  You can find a list with all the needed properties here [application-template.yml](./src/main/resources/application-template.yml)

* Check the project runs

```
java -jar target/copado-deployment-service-1.0.0-spring-boot.jar -help
```

​	After run this command, you will be able to see the project help usage in the command line.



### Service implementation

This is a Spring based project, and some service implementations can be accomplished to deeper customization. Following service interfaces must be implemented to project customization:

| Service                                                      | Description                                                  | Bean                        |
| ------------------------------------------------------------ | ------------------------------------------------------------ | --------------------------- |
| copado.onpremise.service.git.GitService                      | Git client that allows to connect and execute actions in your git repository. | gitService                  |
| copado.onpremise.service.credential.SalesforceCredentialService | Service that allows to retrieve the   credentials for your organizations. | salesforceCredentialService |
| copado.onpremise.service.validation.ValidationService        | Service that will allow or deny the deployment.              | validationService           |

All of these services includes a default implementation into the given project.



## Running the tests

To run the automated tests for this system, run the following command

```
mvn verify
```



## Deployment

To deploy this project on a live system, you need to follow this steps:

### Build and package the project. 

To build and package the project, run the following command

```
mvn clean install spring-boot:repackage
```

This will auto-generate a `jar` file in the `target` folder. For example, for version `1.0.0` the generated file will be:

```target/copado-deployment-service-1.0.0-spring-boot.jar ```



### Deploy

Copy and paste the jar into your production environment and set the environment variables in your system or add a application.yml in your classpath



## Built With

* [Maven](https://maven.apache.org/) - Dependency Management


## Versioning

For the versions available, see the [tags on this repository](https://github.com/CopadoSolutions/copado-deployment-service/tags). 


