package org.jboss.gm.analyzer.alignment;

import org.apache.commons.io.FileUtils;
import org.jboss.gm.analyzer.alignment.TestUtils.TestManipulationModel;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.RestoreSystemProperties;
import org.junit.contrib.java.lang.system.SystemOutRule;
import org.junit.rules.TemporaryFolder;
import org.junit.rules.TestRule;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.Collections;

import static junit.framework.TestCase.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

public class SimpleProjectFunctionalTest {//extends AbstractWiremockTest {

//    @Rule
//    public final SystemOutRule systemOutRule = new SystemOutRule().enableLog();//.muteForSuccessfulTests();
//
//    @Rule
//    public final TestRule restoreSystemProperties = new RestoreSystemProperties();
//
    @Rule
    public TemporaryFolder tempDir = new TemporaryFolder();

    @Before
    public void setup() throws IOException, URISyntaxException {
//        stubFor(post(urlEqualTo("/da/rest/v-1/reports/lookup/gavs"))
//                .willReturn(aResponse()
//                        .withStatus(200)
//                        .withHeader("Content-Type", "application/json;charset=utf-8")
//                        .withBody(readSampleDAResponse("simple-project-da-response.json"))));

    }

    @Test
    public void ensureAlignmentFileCreated() throws IOException, URISyntaxException {
        final File projectRoot = tempDir.newFolder("simple-project");

        final TestManipulationModel alignmentModel = TestUtils.align(
                projectRoot, projectRoot.getName(),
                Collections.singletonMap("dependencyOverride.com.yammer.metrics:*@org.acme.gradle:root", ""));
    }
}
