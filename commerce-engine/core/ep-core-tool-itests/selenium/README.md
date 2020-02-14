**Running Tests:**
mvn clean install -Dcucumber.options="--tags @epCoreTool"

*Updating Browser Driver Versions*
* You can download the latest browser driver from web. e.g. chromedriver.
* Update the RepositoryMap.xml for the driver version.
* Has value can be found locally if you run following in bash command locally.
```
openssl sha1 <filename>
```
* Example: https://github.com/Ardesco/Selenium-Maven-Template/blob/master/src/test/resources/RepositoryMap.xml