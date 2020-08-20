#RepositoryMap.xml

This file contains information that is used to download the requisite selenium driver binaries.

* Add the following dependency and plugins to the project pom:
```
<dependency>
    <groupId>com.elasticpath</groupId>
    <artifactId>ep-test-resources</artifactId>
    <version>${project.version}</version>
</dependency>
```

```
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-dependency-plugin</artifactId>
    <executions>
        <execution>
            <id>unpack-test-resources</id>
            <phase>process-resources</phase>
            <goals>
                <goal>unpack</goal>
            </goals>
            <configuration>
                <artifactItems>
                    <artifactItem>
                        <groupId>com.elasticpath</groupId>
                        <artifactId>ep-test-resources</artifactId>
                        <overWrite>true</overWrite>
                        <outputDirectory>${project.build.testOutputDirectory}</outputDirectory>
                        <includes>RepositoryMap.xml</includes>
                    </artifactItem>
                </artifactItems>
            </configuration>
        </execution>
    </executions>
</plugin>
<plugin>
    <groupId>com.lazerycode.selenium</groupId>
    <artifactId>driver-binary-downloader-maven-plugin</artifactId>
    <version>1.0.11</version>
    <configuration>
        <downloadedZipFileDirectory>${project.build.directory}/webdriver/zips</downloadedZipFileDirectory>
        <rootStandaloneServerDirectory>${project.build.directory}/webdriver/binaries</rootStandaloneServerDirectory>
        <onlyGetDriversForHostOperatingSystem>true</onlyGetDriversForHostOperatingSystem>
        <customRepositoryMap>${project.build.testOutputDirectory}/RepositoryMap.xml</customRepositoryMap>
    </configuration>
    <executions>
        <execution>
            <id>download-webdriver-binaries</id>
            <phase>generate-resources</phase>
            <goals>
                <goal>selenium</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

* To update browser driver versions
    * Download the latest browser driver from web. e.g. chromedriver.
    * Update the RepositoryMap.xml for the driver version and hash value.

* Hash value can be found locally if you run following in bash command locally.
```
openssl sha1 <filename>
```

* RepositoryMap.xml examples:

    * https://github.com/Ardesco/driver-binary-downloader-maven-plugin
    * https://github.com/Ardesco/Selenium-Maven-Template/blob/master/src/test/resources/RepositoryMap.xml
    