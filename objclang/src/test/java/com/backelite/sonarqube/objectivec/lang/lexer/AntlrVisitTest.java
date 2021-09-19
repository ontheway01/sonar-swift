package com.backelite.sonarqube.objectivec.lang.lexer;

import com.backelite.sonarqube.objectivec.internal.ObjectiveCLexer;
import com.backelite.sonarqube.objectivec.internal.ObjectiveCParser;
import com.backelite.sonarqube.objectivec.issues.antlr.AntlrContext;
import com.backelite.sonarqube.objectivec.issues.antlr.AntlrUtils;
import com.backelite.sonarqube.objectivec.issues.antlr.CustomTreeVisitor;
import com.backelite.sonarqube.objectivec.lang.ObjectiveCAstScanner;
import com.backelite.sonarqube.objectivec.lang.api.ObjectiveCMetric;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.apache.commons.io.IOUtils;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Test;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.squidbridge.api.SourceFile;

import java.io.File;
import java.io.IOException;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;

public class AntlrVisitTest {

    @Test
    public void visit() throws IOException {

        final CharStream charStream = CharStreams.fromStream(Objects.requireNonNull(this.getClass().getResourceAsStream("/NetworkRequest.m")));
        final ObjectiveCLexer lexer = new ObjectiveCLexer(charStream);
        lexer.removeErrorListeners();
        final CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        final ObjectiveCParser parser = new ObjectiveCParser(stream);
        parser.removeErrorListeners();
        final ParseTree root = parser.translationUnit();

        CustomTreeVisitor customTreeVisitor = new CustomTreeVisitor();
//        customTreeVisitor.visit(root);
        AntlrContext antlrContext = AntlrUtils.getRequest(IOUtils.toString(Objects.requireNonNull(this.getClass().getResourceAsStream("/NetworkRequest.m")), "UTF-8"));
        customTreeVisitor.visit(root);
//        customTreeVisitor.fillContext(null, antlrContext);
    }
}
