package org.openhab.tools.analysis.tools.test;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.descriptor.PluginDescriptor;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Test;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Properties;

import org.mockito.ArgumentCaptor;

import com.google.inject.internal.util.ImmutableList;

public class CheckstyleCheckerTest {
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID = "org.apache.maven.plugins";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID = "maven-checkstyle-plugin";
    private static final String MAVEN_CHECKSTYLE_PLUGIN_GOAL = "checkstyle";
    private static final String PATH_TO_GENERATED_RULES_XML = Paths.get("src", "test", "resources", "checkstyle")
            .toAbsolutePath().toString();
    private static final String DEFAULT_RULE_SET_XML = "rulesets/checkstyle/rules.xml";
    private static final String CHECKSTYLE_RULE_SET_PROPERTY = "checkstyle.config.location";
    private static final String DEFAULT_FILTER_XML = "rulesets/checkstyle/suppressions.xml";
    private static final String CHECKSTYLE_SUPPRESSION_PROPERTY = "checkstyle.suppressions.location";

    @Test
    public void shouldSkipCheckstyleChecksIfSkipPropertyIsSetToTrue() throws MojoExecutionException {
        CheckstyleCheckerMock checker = spy(CheckstyleCheckerMock.class);
        checker.setIsCheckstyleSkipped(true);
        checker.execute();
        verify(checker, never()).executeCheck(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void shouldExecuteChecksWhenSkipPropertyIsSetToFalse() throws MojoExecutionException {
        Properties properties = mock(Properties.class);
        CheckstyleCheckerMock checker = setUpExecutionCheck(properties);
        PluginDescriptor plugin = mock(PluginDescriptor.class);
        when(plugin.getVersion()).thenReturn("version");
        checker.setPluginDescriptor(plugin);

        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to see if the executeCheck method is called with the proper
            // parameters
        } finally {
            verify(checker).executeCheck(eq(MAVEN_CHECKSTYLE_PLUGIN_GROUP_ID), eq(MAVEN_CHECKSTYLE_PLUGIN_ARTIFACT_ID),
                    eq("version"), eq(MAVEN_CHECKSTYLE_PLUGIN_GOAL), any(), any());
        }
    }

    @Test
    public void shouldProperlySetCheckstyleRulesXmlLocation() throws MojoExecutionException {
        Properties properties = mock(Properties.class);
        CheckstyleCheckerMock checker = setUpExecutionCheck(properties);
        Path currentRelativePath = Paths.get("src", "test", "resources", "checkstyle");
        String directory = currentRelativePath.toAbsolutePath().toString();

        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to see if the executeCheck method is called with the proper
            // parameters
        } finally {
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(properties, atLeastOnce()).setProperty(keyCaptor.capture(), valueCaptor.capture());
            Assert.assertTrue(keyCaptor.getAllValues().contains(CHECKSTYLE_RULE_SET_PROPERTY));
            Assert.assertTrue(valueCaptor.getAllValues().contains(directory + File.separator + "checkstyle-rules.xml"));
        }
    }

    @Test
    public void shouldProperlySetSupressionsLocation() throws MojoExecutionException {
        Properties properties = mock(Properties.class);
        CheckstyleCheckerMock checker = setUpExecutionCheck(properties);
        doReturn("supressions").when(checker).getLocation(any(), eq(DEFAULT_FILTER_XML));
        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to see if the executeCheck method is called with the proper
            // parameters
        } finally {
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            ArgumentCaptor<String> valueCaptor = ArgumentCaptor.forClass(String.class);
            verify(properties, atLeastOnce()).setProperty(keyCaptor.capture(), valueCaptor.capture());
            Assert.assertTrue(keyCaptor.getAllValues().contains(CHECKSTYLE_SUPPRESSION_PROPERTY));
            Assert.assertTrue(valueCaptor.getAllValues().contains("supressions"));
        }
    }

    @Test
    public void shouldGenerateAnXmlFile() throws MojoExecutionException, IOException {
        CheckstyleCheckerMock checker = setUpGeneratingRulesXmlCheck();
        checker.setSkippedCheckTypes(new ArrayList<String>());
        checker.setIsCheckstyleSkipped(false);
        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to check if an xml file is generated
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "checkstyle-rules.xml");
            Assert.assertTrue(file.exists());
            Files.delete(file.toPath());
        }
    }

    @Test
    public void shouldHaveNoTypeNodesInTheGeneratedXmlFile() throws DocumentException, IOException {
        CheckstyleCheckerMock checker = setUpGeneratingRulesXmlCheck();
        checker.setSkippedCheckTypes(new ArrayList<String>());
        checker.setIsCheckstyleSkipped(false);
        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to check if an xml file is generated
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "checkstyle-rules.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(file.toURI().toURL());
            Assert.assertTrue(document.selectNodes("//type").isEmpty());
            Files.delete(file.toPath());
        }
    }

    @Test
    public void shouldProperlyFilterChecksWhenSkippedCheckTypesIsSet()
            throws DocumentException, IOException, MojoExecutionException {
        CheckstyleCheckerMock checker = setUpGeneratingRulesXmlCheck();
        checker.setSkippedCheckTypes(ImmutableList.of("manifest"));
        checker.setIsCheckstyleSkipped(false);
        doReturn(Paths.get("src", "test", "resources", "checkstyle", "filter-test.xml").toUri().toURL().toString())
                .when(checker).getLocation(any(), eq(DEFAULT_RULE_SET_XML));
        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to check if an xml file is generated
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "checkstyle-rules.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(file.toURI().toURL());

            // the root module and the only non-skipped type module
            Assert.assertEquals(document.selectNodes("//module").size(), 2);
            Files.delete(file.toPath());
        }
    }

    @Test
    public void shouldProperlyFilterChecksWhenSkippedCheckTypesIsSetWithMultipleValues()
            throws DocumentException, IOException, MojoExecutionException {
        CheckstyleCheckerMock checker = setUpGeneratingRulesXmlCheck();
        checker.setSkippedCheckTypes(ImmutableList.of("manifest", "properties"));
        checker.setIsCheckstyleSkipped(false);
        doReturn(Paths.get("src", "test", "resources", "checkstyle", "filter-test2.xml").toUri().toURL().toString())
                .when(checker).getLocation(any(), eq(DEFAULT_RULE_SET_XML));
        try {
            checker.execute();
        } catch (Exception e) {
            // We only want to check if an xml file is generated
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "checkstyle-rules.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(file.toURI().toURL());

            // the root module and the only non-skipped type module
            Assert.assertEquals(document.selectNodes("//module").size(), 2);
            Files.delete(file.toPath());
        }
    }

    private CheckstyleCheckerMock setUpGeneratingRulesXmlCheck() {
        CheckstyleCheckerMock checker = spy(CheckstyleCheckerMock.class);
        MavenProject project = mock(MavenProject.class);
        when(project.getProperties()).thenReturn(new Properties());
        when(project.getModel()).thenReturn(mock(Model.class));
        when(project.getModel().getBuild()).thenReturn(mock(Build.class));
        when(project.getModel().getBuild().getDirectory()).thenReturn(PATH_TO_GENERATED_RULES_XML);
        checker.setMavenProject(project);

        return checker;
    }

    private CheckstyleCheckerMock setUpExecutionCheck(Properties properties) {
        CheckstyleCheckerMock checker = spy(CheckstyleCheckerMock.class);
        checker.setIsCheckstyleSkipped(false);
        checker.setSkippedCheckTypes(new ArrayList<String>());
        checker.setCheckstyleMavenVersion("version");
        MavenProject project = mock(MavenProject.class);
        when(project.getProperties()).thenReturn(properties);
        when(project.getModel()).thenReturn(mock(Model.class));
        when(project.getModel().getBuild()).thenReturn(mock(Build.class));
        when(project.getBasedir()).thenReturn(Paths.get("").toFile());
        Path currentRelativePath = Paths.get("src", "test", "resources", "checkstyle");
        String directory = currentRelativePath.toAbsolutePath().toString();
        when(project.getModel().getBuild().getDirectory()).thenReturn(directory);
        checker.setMavenProject(project);

        return checker;
    }
}
