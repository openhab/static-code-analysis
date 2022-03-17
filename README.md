# Static Code Analysis Tool

[![GitHub Actions Build Status](https://github.com/openhab/static-code-analysis/actions/workflows/ci-build.yml/badge.svg?branch=main)](https://github.com/openhab/static-code-analysis/actions/workflows/ci-build.yml)
[![Jenkins Build Status](https://ci.openhab.org/job/static-code-analysis/badge/icon)](https://ci.openhab.org/job/static-code-analysis/)
[![EPL-2.0](https://img.shields.io/badge/license-EPL%202-green.svg)](https://opensource.org/licenses/EPL-2.0)
[![Bountysource](https://www.bountysource.com/badge/tracker?tracker_id=56481698)](https://www.bountysource.com/teams/openhab/issues?tracker_ids=56481698)

The Static Code Analysis Tools is a Maven plugin that executes the Maven plugins for SpotBugs, Checkstyle and PMD and generates a merged .html report.
It is especially designed for openHAB to respect the defined coding guidelines.

This project contains:

 - properties files for the PMD, Checkstyle and SpotBugs Maven plugins configuration in the `sat-plugin/src/main/resources/configuration` folder;
 - rule sets for the plugins in the `sat-plugin/src/main/resources/rulesets` folder;
 - custom rules for PMD, CheckStyle and SpotBugs and unit tests for the rules;
 - tool that merges the reports from the individual plugins in a summary report.

## Essentials

1. [A list of included checks.](docs/included-checks.md)
2. [How to use and configure the Static Analysis Tool.](docs/maven-plugin.md)
3. [How to integrate a new check into the tool.](docs/implement-check.md)

## 3rd Party

- The example checks provided in the `static-code-analysis-config` (`MethodLimitCheck`, `CustomClassNameLengthDetector`, `WhileLoopsMustUseBracesRule`) are based on tutorials how to use the API of Checkstyle, SpotBugs and PMD. For more info, see javadoc;
- The tool that merges the individual reports is based completely on source files from the https://github.com/MarkusSprunck/static-code-analysis-report that are distributed under a custom license. More information can be found in the [LICENSE](LICENSE) file.
