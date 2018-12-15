# Bill Of Materials

This module is used for the management of dependencies used in the rest of this project.
Version changes to dependencies should be made in the `pom.xml` file of this module.
New dependencies used in this project should also be added there.


## Upgrading Dependency Version Using Versions Maven Plugin

Included in this module is the `Versions Maven Plugin` that can help with keeping dependencies up to date.
The plugin can be used to create a report of dependencies with new version.
It can also be used to update the versions for you.
You can exclude dependencies from the report and auto-update by configuring the plugin or the `maven-version-rules.xml`

### Generate Report

By default, the plugin is configured to generate a report of new incremental versions of dependencies.

To generate a report, run from the project root:

`mvn validate -Pcheck-dep-versions`

For only minor version:

`mvn validate -Pcheck-dep-versions -Dversion.allowMinor=true`

For only major versions:

`mvn validate -Pcheck-dep-versions -Dversion.allowMajor=true`

For any versions:

`mvn validate -Pcheck-dep-versions -Dversion.allowAny=true`

### Update Dependencies

By default, the plugin is configured to only update new incremental versions of dependencies.

To update dependencies, run from the project root:

`mvn validate -Pupdate-dep-versions`

For only minor version:

`mvn validate -Pupdate-dep-versions -Dversion.allowMinor=true`

For only major versions:

`mvn validate -Pupdate-dep-versions -Dversion.allowMajor=true`