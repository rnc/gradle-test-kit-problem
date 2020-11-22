package org.jboss.gm.analyzer.alignment;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.TaskAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The actual Gradle task that creates the {@code manipulation.json} file for the whole project
 * (whether it's a single or multi module project)
 */
public class AlignmentTask extends DefaultTask {

    public static final String NAME = "generateAlignmentMetadata";

    private final Logger logger = LoggerFactory.getLogger( getClass());

    @TaskAction
    public void perform() {
        logger.info("###::task::perform::1");
    }

}
