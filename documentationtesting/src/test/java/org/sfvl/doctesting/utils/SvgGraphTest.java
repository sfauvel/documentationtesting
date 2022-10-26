package org.sfvl.doctesting.utils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.sfvl.codeextraction.CodeExtractor;
import org.sfvl.doctesting.junitextension.ApprovalsExtension;
import org.sfvl.doctesting.junitextension.SimpleApprovalsExtension;
import org.sfvl.printer.SvgGraph;

import java.util.Arrays;

@DisplayName("Svg graph")
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


    @Test
    public void specify_grid_size() {
        // >>>
        final String svg = new SvgGraph()
                .withHeight(100)
                .withWidth(200)
                .generate();
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
        doc.write("By default, the factor apply for axis is calculated to fill the space.",
                "This factor could be set manually.",
                "It can be use to display several graph with the same scale.");
        {
            // >>>svg1
            final String svg = new SvgGraph()
                    .withXFactor(290) // 580 / 2 => grid width / nb values
                    .withYFactor(10) // 380 / 38 => grid height / max value
                    .withLine("Values", Arrays.asList(10, 38, 20)).generate();
            // <<<svg1
            doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("svg1")));
            write_svg(svg);
        }

        int max_value = 38;
        int factor = 4;
        int height = 160;
        doc.write("",
                String.format("Here, with a max value at %d and a factor at %d, the max point is %d * %d = %d",
                max_value, factor, max_value, factor, max_value * factor),
                String.format("This point is only %d pixels below the height of the grid specified (%d).", height - max_value * factor, height));
        {
            // >>>svg2
            final String svg = new SvgGraph()
                    .withHeight(160)
                    .withYFactor(4)
                    .withLine("Values", Arrays.asList(10, 38, 20)).generate();
            // <<<svg2

            doc.write(doc.getFormatter().sourceCode(CodeExtractor.extractPartOfCurrentMethod("svg2")));
            write_svg(svg);
        }
    }

    private void write_svg(String svg) {
        doc.write("++++", svg, "++++");
    }
}
