The full info about running performance tests can be found on https://documentation.elasticpath.com/.

##### To run performance locally, as an EP developer, follow these steps:

1. Build the whole ep-commerce project, if not already built

2. Build **performance-tests** module
    ```mvn clean install -f extensions/system-tests/performance-tests/pom.xml ```

3. Ensure the latest Maven settings.xml is deployed to local ".m2" folder 

4. Run performance tests
    ```mvn clean install -f extensions/system-tests/performance-tests/cucumber/pom.xml -P run-performance-tests,compare-with-remote-metabase```

5. Check results by opening the full HTML report from
    **extensions/system-tests/performance-tests/cucumber/target/performance-html-report/fullPerformanceReport.html**
    The same file path is printed in the console on test completion in the following lines:
    ========================================================================================================
    Use browser to open ${project.build.directory}/performance-html-report/fullPerformanceReport.html
    ========================================================================================================
    
##### To run performance locally, as a customer developer, follow these steps:

1. Build the whole ep-commerce project, if not already built

2. Build **performance-tests** module
    ```mvn clean install -f extensions/system-tests/performance-tests/pom.xml ```

3. Ensure you are connected to VPN
    
4. Run performance tests
    ```mvn clean install -f extensions/system-tests/performance-tests/cucumber/pom.xml -P run-performance-tests,compare-with-imported-metabase```
    
5. Check results by openning the full HTML report from
    **extensions/system-tests/performance-tests/cucumber/target/performance-html-report/fullPerformanceReport.html**
    The same file path is printed in the console on test completion in the following lines:
    ========================================================================================================
    Use browser to open ${project.build.directory}/performance-html-report/fullPerformanceReport.html
    ========================================================================================================

6. Customer-specific tests can be added to
    **extensions/system-tests/performance-tests/cucumber/test/resources/features/performance/extensions**
    
##### Things to consider:

1. Cucumber scenario names **must** not contain commas, single, and double quotes
2. The modification of existing (OOTB) tests will affect the metabase history 
   (all performance results are stored in the so called metabase where the results can be filtered and visualized)
3. New tests must be checked for variability.  
   The produced numbers must be (or within 5% deviation) constant across the runs.
   
   Also, the total time for running all tests should not exceed the slowest stage in the build pipeline (15-20 min).
4. Each scenario step should assert the output against expected value to avoid false positives   
   (e.g. a request completed with an error means that the happy path was not executed, just a part of it)  
     									   