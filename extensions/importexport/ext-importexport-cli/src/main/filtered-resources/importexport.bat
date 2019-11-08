@echo off
rem Windows XP Syntax
set LIB=${dependency.directory}
if ["%PROFILE%"]==[""] (
    SET PROFILE=default
)
"%JAVA_HOME%\bin\java" -Dspring.profiles.active="%PROFILE%" -cp ".;%LIB%\*;%IE_CLASSPATH%" -Xmx512m com.elasticpath.importexport.client.Index %*
@echo on
