package org.sfvl.printer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;

import java.util.Arrays;

public class SvgGraphTest {
    @RegisterExtension
    static ApprovalsExtension doc = new SimpleApprovalsExtension();

    @DisplayName("Generate an empty grid")
    @Test
    public void an_empty_grid() {
        // >>>
        final String svg = new SvgGraph().generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    @DisplayName("Draw a single line")
    @Test
    public void one_simple_line() {
        // >>>
        final String svg = new SvgGraph()
                .withLine("Values", Arrays.asList(0, 20, 10))
                .generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    @DisplayName("Draw a single line with negative values")
    @Test
    public void one_simple_line_with_negative_value() {
        // >>>
        final String svg = new SvgGraph()
                .withLine("Values", Arrays.asList(0, -20, -10))
                .generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    @DisplayName("Draw a single line with negative and positive values")
    @Test
    public void one_line_with_negative_and_postive_values() {
        // >>>
        final String svg = new SvgGraph()
                .withLine("Values", Arrays.asList(-15, 4, -6, 30, 25))
                .generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    @DisplayName("Draw several lines")
    @Test
    public void multi_lines() {
        // >>>
        final String svg = new SvgGraph()
                .withLine("Line A", Arrays.asList(-15, 6, 30, 25))
                .withLine("Line B", Arrays.asList(5, 12, 45, 17))
                .withLine("Line C", Arrays.asList(-5, -10, -20, 8))
                .generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    @Test
    public void apply_factor_on_axes() {
        // >>>
        final String svg = new SvgGraph()
                .withXFactor(290) // 580 / 2 => grid width / nb values
                .withYFactor(10) // 380 / 38 => grid height / max value
                .withLine("Values", Arrays.asList(10, 38, 20)).generate();
        // <<<

        doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod()));
        write_svg(svg);
    }

    private void write_svg(String svg) {
        doc.write("++++", svg, "++++");
    }
}
