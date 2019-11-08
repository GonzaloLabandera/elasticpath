# Cortex Cucumber Tests

Cucumber is a tool that supports Behavior Driven Development (BDD). It offers a way to write tests that anybody can understand, regardless of their technical knowledge.

Cucumber tests can be run with either OAuth2 (the default) or header authentication.  They can also be run using the built-in apps/H2 database (the default) or using a remote Cortex instance.

### Running tests using Command Line

By default, tests are run against built-in apps using a H2 database with OAuth2 authorization

To run tests using in-memory H2 database navigate to `...extensions/cortex/system-tests` and execute the following command
```
mvn clean install
```

This is equivalent to the following command
```
mvn clean install -Dep.rest.authtype="OAuth2"
```

To run a subset of tests based on their tags (eg. `@addresses` )
```
mvn clean install -Dcucumber.options="--tags @addresses"
```

For the commands below:
* navigate to `...extensions/cortex/system-tests/cucumber`
* if on **Windows** make sure you remove `\` on the option `\!setup-local-integration-test`.

To run tests locally
```
mvn clean install -Dep.rest.baseurl="http://localhost:9080/cortex" -Dep.jms.url="tcp://localhost:61616" -P \!setup-local-integration-test
```

To run tests against a remote server
```
mvn clean install -Dep.rest.baseurl="http://<SERVER_URL>:<CORTEX_PORT>/cortex" -Dep.jms.url="tcp://<SERVER_URL>:<JMS_PORT>" -P \!setup-local-integration-test
```

The options above can be combined, eg. to run a subset of tests against a remote server
```
mvn clean install -Dep.rest.baseurl="http://<SERVER_URL>:<CORTEX_PORT>/cortex" -Dep.jms.url="tcp://<SERVER_URL>:<JMS_PORT>" -P \!setup-local-integration-test -Dcucumber.options="--tags @<TAG_NAME>"
```

##### Header Authentication

Many out-of-the-box tests do not support header authentication.  

Use the following command to run the out-of-the-box header authentication friendly tests only using the default built-in apps/H2 database in header authentication
```
mvn clean install -Dep.rest.authtype="headerAuth" -Dcucumber.options="--tags @headerAuth"
```

### Running tests using IntelliJ

1. Build `.../extensions/cortex/system-tests` module without running integration tests
```
mvn clean install -DskipAllTests
```

2. Edit the default Cucumber Java configuration and the glue copied from `CucumberRunnerIT` to the default configuration. Start cortex locally, and  then simply right click in the feature file and run the test. 
e.g. the glue in configuration should be
```
classpath:com.elasticpath.cortex.dce
classpath:com.elasticpath.cucumber
classpath:com.elasticpath.jms.cucumber
classpath:com.elasticpath.repo.cucumber
```

2. Edit `CucumberRunnerIT` runner configurations and add the required properties to your VM Options

    eg. to run tests with tag `@addresses` you would have VM Options: `-Dcucumber.options="--tags @addresses"`

### Test Failures

Cucumber test reports can be found in `.../cucumber/target/cucumber-html-report/index.html`

##### JWT authorization

If you see failures for tests with tag `@jwtAuthorization`

Navigate to `.../ep-commerce/extensions/database` and execute the following command:
```
mvn clean install -Pupdate-conf
```
