package org.openhab.tools.analysis.tools.test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.junit.Test;

public class SpotbugsCheckerTest {
    @Test
    public void shouldSkipSpotbugsChecksIfSkipPropertyIsSetToTrue() throws MojoExecutionException, MojoFailureException {
        SpotbugsCheckerMock checker = spy(SpotbugsCheckerMock.class);
        checker.setIsSpotbugsSkipped(true);
        checker.execute();
        verify(checker, never()).executeCheck(any(), any(), any(), any(), any(), any());
    }
}
