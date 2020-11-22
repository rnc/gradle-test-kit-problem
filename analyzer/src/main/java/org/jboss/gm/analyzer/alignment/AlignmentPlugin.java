package org.jboss.gm.analyzer.alignment;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Results in adding a task with name {@value org.jboss.gm.analyzer.alignment.AlignmentTask#NAME}.
 * It scans the project(s) and creates the manipulation models.
 */
@SuppressWarnings("unused")
public class AlignmentPlugin implements Plugin<Project> {

    private final Logger logger = LoggerFactory.getLogger( getClass());

    static {
        System.out.println("Running Gradle Alignment Plugin (GME) ");
    }

    @Override
    public void apply(Project project) {
        // we need to create an empty alignment file at the project root
        // this file will then be populated by the alignment task of each project
        logger.warn("### apply::1");

        project.getTasks().create(AlignmentTask.NAME, AlignmentTask.class);
        logger.warn("### apply::2::FINISHED");
    }
}
