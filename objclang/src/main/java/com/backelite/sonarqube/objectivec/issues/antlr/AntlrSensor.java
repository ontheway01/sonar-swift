package com.backelite.sonarqube.objectivec.issues.antlr;

import com.backelite.sonarqube.objectivec.lang.core.ObjectiveC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class AntlrSensor implements Sensor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AntlrSensor.class);
    private static final int EXECUTOR_TIMEOUT = 10000;

    @Override
    public void describe(SensorDescriptor sensorDescriptor) {
        sensorDescriptor
                .onlyOnLanguage(ObjectiveC.KEY)
                .name("Objective-C sensor")
                .onlyOnFileType(InputFile.Type.MAIN);
    }

    @Override
    public void execute(SensorContext sensorContext) {

        FilePredicate hasDart = sensorContext.fileSystem().predicates().hasLanguage(ObjectiveC.KEY);
        FilePredicate isMain = sensorContext.fileSystem().predicates().hasType(InputFile.Type.MAIN);
        FilePredicate isTest = sensorContext.fileSystem().predicates().hasType(InputFile.Type.TEST);
        FilePredicate dartAndMain = sensorContext.fileSystem().predicates().and(hasDart, isMain);
        FilePredicate dartAndTest = sensorContext.fileSystem().predicates().and(hasDart, isTest);
        final Charset charset = sensorContext.fileSystem().encoding();

        final ExecutorService executorService = Executors.newWorkStealingPool();

        for(InputFile inf : sensorContext.fileSystem().inputFiles(dartAndMain)){

            executorService.execute(() -> {
                // Visit source files
                try {
                    final AntlrContext antlrContext = AntlrContext.fromInputFile(inf, charset);
                    CustomTreeVisitor visitor = new CustomTreeVisitor(sensorContext);
                    visitor.visit(antlrContext.getRoot());
//                    visitor.fillContext(sensorContext, antlrContext);
                } catch (IOException e) {
                    LOGGER.warn("Unexpected error while analyzing file " + inf.filename(), e);
                }
            });

        }

        for(InputFile inf : sensorContext.fileSystem().inputFiles(dartAndTest)){

            executorService.execute(() -> {
                // Visit test files (for syntax highlighting only)
                try {
                    final AntlrContext antlrContext = AntlrContext.fromInputFile(inf, charset);
                    CustomTreeVisitor visitor = new CustomTreeVisitor(sensorContext);
                    visitor.visit(antlrContext.getRoot());
//                    visitor.fillContext(sensorContext, antlrContext);
                } catch (IOException e) {
                    LOGGER.warn("Unexpected error while analyzing file " + inf.filename(), e);
                }
            });

        }

        try {
            executorService.shutdown();
            executorService.awaitTermination(EXECUTOR_TIMEOUT, TimeUnit.SECONDS);
            executorService.shutdownNow();
        } catch (final InterruptedException e) {
            LOGGER.warn("Unexpected error while running waiting for executor service to finish", e);
        }

    }
}
