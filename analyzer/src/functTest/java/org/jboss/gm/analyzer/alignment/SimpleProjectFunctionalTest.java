package org.jboss.gm.analyzer.alignment;

import org.jboss.gm.analyzer.alignment.TestUtils.TestManipulationModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import static org.junit.Assert.assertTrue;

public class SimpleProjectFunctionalTest {

//    @Rule
//    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();//.muteForSuccessfulTests();
//
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Test
    public void ensureAlignmentFileCreated() throws IOException, URISyntaxException {
        final File projectRoot = tempDir.newFolder("simple-project");

        final TestManipulationModel alignmentModel = TestUtils.align(
                projectRoot, projectRoot.getName(),
                Collections.singletonMap("dependencyOverride.com.yammer.metrics:*@org.acme.gradle:root", ""));
        assertTrue(alignmentModel.getProject().exists());
    }
}
