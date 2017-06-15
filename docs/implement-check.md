## Write Custom Checks

All of the used static code analysis tools have Java API for writing custom checks.

Checkstyle API is easy to use and to implement checks for different file extensions and languages. Examples are included in the project.

PMD extends this by giving the possibility to define a rule with the XPath syntax. PMD offers even more - a Rule Designer that speeds up the process of developing a new rule! See http://nullpointer.debashish.com/pmd-xpath-custom-rules.

PMD rules with XPatch expressions are defined directly in the rule set (`src/main/resources/rulesets/pmd/xpath`) folder.

Helpful links when writing a custom check for the first time may be:

- for PMD - http://pmd.sourceforge.net/pmd-4.3.0/howtowritearule.html;
- for Checkstyle - http://checkstyle.sourceforge.net/writingchecks.html;
- for FindBugs - https://www.ibm.com/developerworks/library/j-findbug2/.

## Add Tests For The New Checks

You can easily test your custom rules for PMD and Checkstyle.

In order to add a new test for PMD you have to do two things:
- Create a test class in the `src/test/java` folder that extends `SimpleAggregatorTst` and overrides the `setUp()` method;
- Add a .xml file in the `src/test/resources` folder that contains the code to be tested.

Adding a test for Checkstyle is even easier - extend the `BaseCheckTestSupport`.

For more information: https://pmd.github.io/pmd-5.4.1/customizing/rule-guidelines.html.