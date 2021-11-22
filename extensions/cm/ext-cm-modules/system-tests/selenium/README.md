# Prerequisite
  * Build ep-commerce from root, using the following command:
  ```java
  mvn clean install -DskipAllTests
  ```

# cm-selenium-test

**Building Project**

Build the following projects using this command: mvn clean install -DskipAllTests
* commerce/ep-commerce/extensions/cm/ext-cm-modules/system-tests
* commerce/ep-commerce/extensions/cm/ext-cm-modules/system-tests/selenium

**Setting up commerce/ep-commerce/extensions/cm/ext-cm-modules/system-tests Project in IntelliJ**

1. Create Project from Existing Sources using the path above.
2. Select Import project from external model -> Choose maven
3. Select checkboxes for:
    * Search for projects recursively
    * Import Maven projects automatically
4. Click next until Finish.
5. Download the Cucumber.java and Cucumber.groovy plugins in IntelliJ
6. Go to File > Settings > Editor > File type > Cucumber Scenario
    * add *.feature to the Cucumber Scenario file type.
7. Rebuild extensions/cm/ext-cm-modules/system-tests using InteliJ
   (right-click on the folder, select "Rebuild ...." option)    

**Before Running the Tests**

1. Ensure the CM you are testing against has the Test ID mode turned on.
    In ext-cm-webapp-runner, run the following:
    ```
    mvn clean tomcat8:run-war -Dorg.eclipse.rap.rwt.enableUITests=true
    ```
2. All applications (except batch server) are running, thus:
    Cortex, Integration, Search, CM and ActiveMQ.
    
3. If the default browser is Chrome, ensure that Chromedriver (the web driver used by Selenium) has the same version.
   To check Chrome's version, go to Chrome -> Help -> About.
   The Chromedriver is downloaded by Maven, before the test is started, into "system-tests/selenium/target/webdriver".
   The driver may be old and it's recommended to store the driver outside of this folder.
   
   The matching driver can be obtained from https://chromedriver.chromium.org/downloads and specified in the command line
    **-Dselenium.chrome.driver.path=<ABSOLUTE_PATH_TO_CHROMEDRIVER>**

**Running Tests:**

mvn clean install -Dcucumber.options="--tags @regressionTest"

*Test Suites*

* Sanity Tests: @sanityTest
* Smoke Tests: @smokeTest
* Regression Tests: @regressionTest

*Maven Options:*
* -Dcucumber.options="--tags @regressionTest" - You can replace the tag to your own tag.
* -Dfailsafe.fork.count="1" - This is the number of parallel tests at the same time. Default is 1 and can be changed to other values depending on number of TestsIT classes.
* -Premote -Dremote.web.driver.url="<REMOTE DRIVER IP>" - The "remote" triggers tests to be executed using remote VM. The "remote.web.driver.url" specifies the URL of the remote VM. e.g. "http://10.10.2.113:4444/wd/hub"
    * Note: You have to have selenium grid setup in order to use this feature. Please refer to official documentation on Selenium Grid.  

**Running subset of tests:**
* You can run a subset of tests by right clicking and run any one of TestsIT classes under /selenium/src/test/java/com/elasticpath/cucumber/
* You can create your own local runner class to run your own tagged tests. E.g. RunLocalTestsIT.java which runs your own tagged tests @local 
    * Do not commit the local runner class and tags as they are only for your local testing purpose. 

**Debugging Tests:**

Pre-requisites:

1. All applications built
2. All required applications are running
3. To debug a single scenario, remove all other scenarios from the feature file.

To debug a specific test, using Maven, run the following command from the **system-tests/selenium** folder:
```
mvn clean verify -f extensions/cm/ext-cm-modules/system-tests/selenium/pom.xml -Dcucumber.options="src/test/resources/com.elasticpath.cucumber/<PATH_TO_FEATURE_FILE>" 
```

If custom-version Chromedriver is required, add the following parameter:
```
-Dselenium.chrome.driver.path=<ABSOLUTE_PATH_TO_CHROMEDRIVER>
```

If Cortex and CM are running on different ports, use the following parameters:
```
 -Dep.cm.port.http=<CM_PORT> -Dep.rest.baseurl=http://localhost:<CORTEX_PORT>/cortex
```

And to enter the debug mode, use 
```
 -Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=<DEBUG_PORT> -Xnoagent"
```

Here is the example of a command for debugging a single scenario in the OrderReturn.feature.  
All other scenarios were removed.  
The Cortex and CM servers were running on port **8082**.  
A custom Chromedriver, version 87, was used.

```
mvn clean verify -Dcucumber.options="src/test/resources/com.elasticpath.cucumber/customerService/order/orderReturn/OrderReturn.feature" -Dselenium.chrome.driver.path=/home/nradic/projects/projects/m2-origin/master/chrome/chromedriver.87 -Dep.cm.port.http=8082 -Dep.rest.baseurl=http://localhost:8082/cortex -Dmaven.failsafe.debug="-Xdebug -Xrunjdwp:transport=dt_socket,server=y,suspend=y,address=8000 -Xnoagent"
``` 