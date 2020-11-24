package org.jboss.gm.analyzer.alignment;

import org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.gradle.testkit.runner.TaskOutcome;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static junit.framework.TestCase.assertTrue;

public final class TestUtils {

    private TestUtils() {
    }

    static TestManipulationModel align(File projectRoot, String projectDirName, Map<String, String> systemProps)
            throws IOException, URISyntaxException {

        FileUtils.copyDirectory(Paths
                .get(TestUtils.class.getClassLoader().getResource(projectDirName).toURI()).toFile(), projectRoot);
        return align( projectRoot, systemProps);
    }

    /**
     * this method assumes the projectRoot directory already contains the gradle files (usually unpacked from resources)
     *
     * @param projectRoot the root directory of the aligned project
     * @param systemProps the system properties to apply for the alignment run
     * @return the manipulation model
     */
    static TestManipulationModel align( File projectRoot, Map<String, String> systemProps ) {
        assertTrue(projectRoot.toPath().resolve("build.gradle").toFile().exists() ||
                projectRoot.toPath().resolve("build.gradle.kts").toFile().exists());

        final BuildResult buildResult;
        final TaskOutcome outcome;

        final Map<String, String> finalSystemProps = new LinkedHashMap<>();
        if (!systemProps.containsKey("repoRemovalBackup")) {
            finalSystemProps.put("repoRemovalBackup", "settings.xml");
        }
        finalSystemProps.putAll(systemProps);
        final List<String> systemPropsList = finalSystemProps.entrySet().stream()
                .map(e -> "-D" + e.getKey() + "=" + e.getValue())
                .collect(Collectors.toList());
        final List<String> allArguments = new ArrayList<>(systemPropsList.size() + 4);
        allArguments.add("-DgmeFunctionalTest=true"); // Used to indicate for the plugin to reinitialise the configuration.
        allArguments.add("--stacktrace");
        allArguments.add("--debug");
        allArguments.add(AlignmentTask.NAME);
        allArguments.addAll(systemPropsList);

        final GradleRunner runner = GradleRunner.create()
                .withProjectDir(projectRoot)
                .withArguments(allArguments)
        //   .withGradleVersion( "6.4.1" )
        //   .withDebug(true)
                .forwardOutput()
                .withPluginClasspath();

        boolean expectFailure = false;
        if (expectFailure) {
            buildResult = runner.buildAndFail();
            outcome = TaskOutcome.FAILED;
        } else {
            outcome = TaskOutcome.SUCCESS;
            buildResult = runner.build();
        }
        return new TestManipulationModel(projectRoot);
    }


    public static class TestManipulationModel  {

        private final File project;

        public TestManipulationModel( File p) {
            this.project = p;
        }

        public File getProject() {
            return project;
        }
    }
}

