# Usage

Add the following profiles to your pom.xml:

```
  <profile>
    <id>check</id>
    <build>
      <pluginManagement>
        <plugins>
          <plugin>
            <groupId>org.openhab.tools</groupId>
            <artifactId>static-code-analysis</artifactId>
            <version>${sat.version}</version>
            <executions>
              <execution>
                <phase>verify</phase>
                <goals>
                  <goal>checkstyle</goal>
                  <goal>pmd</goal>
                  <goal>findbugs</goal>
                  <goal>report</goal>
                </goals>
              </execution>
            </executions>
          </plugin>
        </plugins>
      </pluginManagement>
    </build>
  </profile>
  <profile>
    <id>check-bundles</id>
    <activation>
      <file>
        <exists>META-INF/MANIFEST.MF</exists>
      </file>
    </activation>
    <build>
      <plugins>
        <plugin>
            <groupId>org.openhab.tools</groupId>
            <artifactId>static-code-analysis</artifactId>
        </plugin>
      </plugins>
    </build>
  </profile>
```

 Execute `mvn clean install -P check` from the root of your project.

 Reports are generated for each module individually and can be found in the `target/code-analysis` directory. The merged report can be found in the root target directory.

 The build will fail if a problem with high priority is found by some of the Maven plugins for PMD, Checkstyle and FindBugs. Each of the plugins has its own way to prioritize the detected problems:

 - for PMD - the build will fail when a rule with Priority "1" is found;
 - for Checkstyle - a rule with severity="Error";
 - for Findbugs - any Matcher with Rank between 1 and 4.

## Maven plugin goals and parameters

**static-code-analysis:pmd**

Description:
    Executes the `maven-pmd-plugin` goal `pmd` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **pmdRuleset** | String | Relative path of the XML configuration to use. If not set the default ruleset file will be used |
| **maven.pmd.version** | String | The version of the maven-pmd-plugin that will be used (Default value is **3.7**)|
| **pmdPlugins** | List<Dependency> | A list with artifacts that contain additional checks for PMD |

**static-code-analysis:checkstyle**

Description:
    Executes the `maven-checkstyle-plugin` goal `checkstyle` with a ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **checkstyleRuleset** | String | Relative path of the XML configuration to use. If not set the default ruleset file will be used |
| **checkstyleFilter** | String | Relative path of the suppressions XML file to use. If not set the default filter file will be used |
| **maven.checkstyle.version** | String | The version of the maven-checkstyle-plugin that will be used (default value is **2.17**)|
| **checkstylePlugins** | List<Dependency> | A list with artifacts that contain additional checks for Checkstyle |
| **checkstyleProperties** | String | Relative path of the properties file to use in the ruleset to configure specific checks |

**static-code-analysis:findbugs**

Description:
    Executes the `findbugs-maven-plugin` goal `findbugs` with a  ruleset file and configuration properties

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **findbugsRuleset** | String | Relative path to the XML that specifies the bug detectors which should be run. If not set the default file will be used|
| **findbugsInclude** | String | Relative path to the XML that specifies the bug instances that will be included in the report. If not set the default file will be used|
| **findbugsExclude** | String | Relative path to the XML that specifies the bug instances that will be excluded from the report. If not set the default file will be used|
| **findbugs.maven.version** | String | The version of the findbugs-maven-plugin that will be used (default value is **3.0.4**)|
| **findbugs.version** | String | The version of FindBugs that will be used (default value is **3.0.1**)|
| **findbugsPlugins** | List<Dependency> | A list with artifacts that contain additional detectors/patterns for FindBugs |
| **findbugs.slf4j.version** | String | The version of the findbugs-slf4j plugin that will be used (default value is **1.2.4**)|

**static-code-analysis:report**

Description:
    Transforms the results from FindBugs, Checkstyle and PMD into a single HTML Report with XSLT

Parameters:

| Name | Type| Description |
| ------ | ------| -------- |
| **report.targetDir** | String | The directory where the individual report will be generated (default value is **${project.build.directory}/code-analysis**) |
| **report.summary.targetDir** | String | The directory where the summary report, containing links to the individual reports will be generated (Default value is **${session.executionRootDirectory}/target**)|
| **report.fail.on.error** | Boolean | Describes of the build should fail if high priority error is found (Default value is **true**)|

## Customization

Different sets of checks can be executed on different types of projects.

The tool executes different checks on OSGi bundles and ESH Bindings. It uses default configuration files for FindBugs, Checkstyle and PMD that are stored in the `src/main/resources/configuration`.

If you want to use a custom set of rules you will have to set the configuration parameters for the individual MOJOs. An example configuration may look like this;

```
  <plugin>
    <groupId>org.openhab.tools</groupId>
    <artifactId>static-code-analysis</artifactId>
    <configuration>
      <checkstyleRuleset>build-tools/checkstyle/binding.xml</checkstyleRuleset>
      <checkstyleFilter>build-tools/checkstyle/suppressions.xml</checkstyleFilter>
      <pmdRuleset>build-tools/pmd/binding.xml</pmdRuleset>
      <findbugsInclude>build-tools/findbugs/binding.xml</findbugsInclude>
      <findbugsExclude>build-tools/findbugs/exclude.xml</findbugsExclude>
      <findbugsRuleset>build-tools/findbugs/visitors.xml</findbugsRuleset>
    </configuration>
  </plugin>
```

Information about the syntax of the configuration files (except the `visitors.xml`) can be found on the web pages of the individual plugins.

The `visitors.xml` contains a list with FindBugs visitors (bug detectors) and has the following syntax:

```
<?xml version="1.0" encoding="UTF-8"?>
<visitors>
  <visitor>AtomicityProblem</visitor>
  ...
<visitors/>
```

### Individual plugin customization

Each of the Maven plugins that are used (for FindBugs, Checkstyle and PMD) are configured by setting user properties that are located in the `src/main/resources/configuration` directory.

You can refer to the following links for more configuration options for the specific Maven plugins:

- https://maven.apache.org/plugins/maven-pmd-plugin/check-mojo.html;
- https://maven.apache.org/plugins-archives/maven-checkstyle-plugin-2.16/checkstyle-mojo.html;
- http://gleclaire.github.io/findbugs-maven-plugin/check-mojo.html.

## Reuse Checks

PMD, Checkstyle and FindBugs come with a set of custom rules that can be used directly in a rule set.

Helpful resources with lists of the available checks and information how to use them:

- for PMD - https://pmd.github.io/pmd-5.4.0/pmd-java/rules/index.html;
- for Checkstyle - http://checkstyle.sourceforge.net/checks.html;
- for FindBugs - Keep in mind that the process for adding a check in FindBugs contains two steps:
   - First you should open the link with [BugDescriptors](http://findbugs.sourceforge.net/bugDescriptions.html), choose the bug that you want to detect and create a Match in `src/main/resources/rulesets/findbugs/YOUR_RULESET`;
   - Next you should find the Detector that finds the Bug that you have selected above (you can use [this list](https://github.com/findbugsproject/findbugs/blob/d1e60f8dbeda0a454f2d497ef8dcb878fa8e3852/findbugs/etc/findbugs.xml)) and add the Detector in the `src/main/resources/configuration/findbugs.properties` under the property `visitors`.