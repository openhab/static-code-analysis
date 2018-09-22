package org.openhab.tools.analysis.tools.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Test;

import com.google.inject.internal.util.ImmutableList;

public class PmdCheckerTest {
    private static final String DEFAULT_RULESET_XML = "rulesets/pmd/rules.xml";
    private static final String PATH_TO_GENERATED_RULES_XML = Paths.get("src", "test", "resources", "pmd")
            .toAbsolutePath().toString();

    @Test
    public void shouldSkipPmdChecksIfSkipPropertyIsSetToTrue() throws MojoExecutionException, MojoFailureException {
        PmdCheckerMock checker = spy(PmdCheckerMock.class);
        checker.setIsPmdSkipped(true);
        checker.execute();
        verify(checker, never()).executeCheck(any(), any(), any(), any(), any(), any());
    }

    @Test
    public void shouldGenerateRulesXml() throws IOException {
        PmdCheckerMock checker = setUpGenerateXmlTest();
        checker.setIsPmdSkipped(false);
        Collection<String> skippedCategories = new ArrayList<>();
        skippedCategories.add("asd");
        checker.setSkippedCategories(skippedCategories);
        try {
            checker.execute();
        } catch (Exception e) {
            // We only check the generated rules xml
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "pmd-rules.xml");
            Assert.assertTrue(file.exists());
            Files.delete(file.toPath());
        }
    }

    @Test
    public void shouldFilterRulesWhenSkippedCategoriesIsSet()
            throws IOException, DocumentException, MojoExecutionException {
        PmdCheckerMock checker = setUpGenerateXmlTest();
        checker.setIsPmdSkipped(false);
        checker.setSkippedCategories(ImmutableList.of("errorprone"));
        doReturn(Paths.get("src", "test", "resources", "pmd", "filter-test.xml").toUri().toURL().toString())
                .when(checker).getLocation(any(), eq(DEFAULT_RULESET_XML));
        try {
            checker.execute();
        } catch (Exception e) {
            // We only check the generated rules xml
        } finally {
            File file = new File(PATH_TO_GENERATED_RULES_XML + File.separator + "pmd-rules.xml");
            SAXReader reader = new SAXReader();
            Document document = reader.read(file.toURI().toURL());

            // the only non-skipped category rule
            Assert.assertEquals(1, document.getRootElement().elements("rule").size());
            Files.delete(file.toPath());
        }
    }

    private PmdCheckerMock setUpGenerateXmlTest() {
        PmdCheckerMock checker = spy(PmdCheckerMock.class);
        MavenProject project = mock(MavenProject.class);
        when(project.getProperties()).thenReturn(new Properties());
        when(project.getModel()).thenReturn(mock(Model.class));
        when(project.getModel().getBuild()).thenReturn(mock(Build.class));
        when(project.getModel().getBuild().getDirectory()).thenReturn(PATH_TO_GENERATED_RULES_XML);
        checker.setMavenProject(project);

        return checker;
    }
}
