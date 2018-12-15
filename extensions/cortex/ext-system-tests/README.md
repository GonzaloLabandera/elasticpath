# Prerequisite
  * build project /ep-commerce/extensions/cortex/system-tests to install required dependency with following command:
```maven
    mvn clean install -DskipAllTests
```

## Import Project in IntelliJ:
1. File -> New -> Project from Existing Sources....
2. Select the /ep-commerce/extensions/cortex/ext-system-tests folder
3. Select Import project from external model option and choose Maven from the list
4. Click Next in the subsequent screens then click Finish

* build project /ep-commerce/extensions/cortex/ext-system-tests using following command:
```maven
    mvn clean install -DskipAllTests
```

## Adding Tests and Step Definitions:
* Create features under test/resources/features/<YOUR_FOLDER>
* Create Step Definitions under test/java/com/elasticpath/cortex/dce/<YOUR_FOLDER>

## Running tests:
From /ep-commerce/extensions/cortex/ext-system-tests use following commands to:

1. run all tests:
```maven
    mvn clean install
```
2. run tests with specific tag:
```maven
    mvn clean install -Dcucumber.options="--tags @example"
```
3. run tests against remote server:
```maven
    mvn clean install -Dep.rest.baseurl="http://<REMOTE_SERVER_URL>/cortex
```