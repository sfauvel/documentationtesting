package org.sfvl.printer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SvgGraph {

    private int width = 700;
    private int height = 500;
    private final int margin = 30;
    private final int marginAxe = 30;
    private final List<SvgLineData> lines = new ArrayList<>();
    private SvgAxe axeX;
    private SvgAxe axeY;

    final static List<String> colors = Arrays.asList("blue", "red", "green");
    private int colorIndex = 0;

    public SvgGraph withXFactor(int factor) {
        axeX = new SvgAxe(factor, margin + marginAxe);
        return this;
    }

    public SvgGraph withYFactor(int factor) {
        axeY = new SvgAxe(-factor, height - (margin + marginAxe));
        return this;
    }

    private SvgGraph withYFactor(double factor, Integer minY) {
        axeY = new SvgAxe(-factor, height - (margin + marginAxe) + (int) (minY * factor));
        return this;
    }

    public SvgGraph withHeight(int height) {
        this.height = height + (2 * margin) + (2 * marginAxe);
        return this;
    }

    public SvgGraph withWidth(int width) {
        this.width = width + (2 * margin) + (2 * marginAxe);
        return this;
    }

    private static class SvgAxe {
        final double factor;
        private int delta;

        public SvgAxe() {
            this(1, 0);
        }

        public SvgAxe(double factor, int delta) {
            this.factor = factor;
            this.delta = delta;
        }

        public int getPosition(int value) {
            return (int) ((value * factor) + delta);
        }

        public double getValue(int position) {
            return (position - delta) / factor;
        }
    }

    private static class SvgLineData {

        private final String name;
        private final List<Integer> lineData;
        private final String color;

        SvgLineData(String name, List<Integer> lineData, String lineColor) {
            this.name = name;
            this.lineData = lineData;
            this.color = lineColor;
        }

        public int size() {
            return this.lineData.size();
        }

        public String getColor() {
            return this.color;
        }
    }

    public SvgGraph() {
    }

    public String generate() {

        return String.join("\n",
                "<!DOCTYPE svg PUBLIC \"-//W3C//DTD SVG 1.1//EN\" \"http://www.w3.org/Graphics/SVG/1.1/DTD/svg11.dtd\">",
                "<svg version=\"1.1\" xmlns=\"http://www.w3.org/2000/svg\"",
                "width=\"" + width + "\" height=\"" + height + "\"     style=\"background-color:grey\">",
                generateStyle(),
                "<defs>",
                "    <marker id=\"markerCircle\" markerWidth=\"8\" markerHeight=\"8\" refX=\"5\" refY=\"5\">",
                "        <circle cx=\"5\" cy=\"5\" r=\"1.5\" style=\"stroke: none; fill:#000000;\"/>",
                "    </marker>",
                "</defs>",
                generateGrid(),
                generateLines(),
                "</svg>");
    }

    private String generateGrid() {
        final Integer maxY = getMaxValue().orElse(0);
        final Integer minY = getMinValue().orElse(0);
        final Integer maxX = lines.stream()
                .mapToInt(line -> line.size() - 1)
                .max()
                .orElse(0);

        final int areaWidth = width - (margin * 2);
        final int areaHeight = height - (margin * 2);
        final int gridWidth = areaWidth - (marginAxe * 2);
        final int gridHeight = areaHeight - (marginAxe * 2);

        int xMinPosition = margin + marginAxe;
        int xMaxPosition = gridWidth + margin + marginAxe;
        int yMinPosition = gridHeight + margin + marginAxe;
        int yMaxPosition = margin + marginAxe;

        if (axeX == null) {
            final int valueWidth = Math.max(1, maxX);
            withXFactor(gridWidth / valueWidth);
        }
        final int minYValue = Math.min(0, minY);
        if (axeY == null) {
            final int valueHeight = Math.max(1, Math.max(0, maxY) - minYValue);
            withYFactor((double) gridHeight / valueHeight, minYValue);
        }
        int x0 = axeX.getPosition(0);
        int y0 = axeY.getPosition(0);

        int yMax = (int) axeY.getValue(yMaxPosition);
        int xMax = (int) axeX.getValue(xMaxPosition);
        return String.join("\n",
                "<svg class=\"graph\">",
                "    <rect fill=\"white\" width=\"" + areaWidth + "\" height=\"" + areaHeight + "\" x=\"" + margin + "\" y=\"" + margin + "\"/>",
                "    <g class=\"grid\">",
                "        " + svgVLine(x0, yMinPosition, yMaxPosition),
                "    </g>",
                "    <g class=\"grid\">",
                "        " + svgHLine(xMinPosition, xMaxPosition, y0),
                "    </g>",
                "",
                numberOnYAxis(x0, yMax),
                numberOnYAxis(x0, minYValue),
                "",
                numberOnXAxis(y0, 0),
                numberOnXAxis(y0, xMax),
                "</svg>");
    }

    private Optional<Integer> getMinValue() {
        return lines.stream()
                .flatMap(line -> line.lineData.stream())
                .min(Integer::compare);
    }

    private Optional<Integer> getMaxValue() {
        return lines.stream()
                .flatMap(line -> line.lineData.stream())
                .max(Integer::compare);
    }

    private String numberOnYAxis(int x0, int value) {
        int y = axeY.getPosition(value);
        return String.join("\n",
                "    " + svgText(margin + 5, y + 5, value),
                "    " + svgHLine(x0 - 4, x0 + 4, y)
        );
    }

    private String numberOnXAxis(int y0, int value) {
        int x = axeX.getPosition(value);
        return String.join("\n",
                "    " + svgText(x - 5, y0 + marginAxe - 5, value),
                "    " + svgVLine(x, y0, y0 + 4)
        );
    }

    private String svgText(int x, int y, int text) {
        return String.format("<text x=\"%d\" y=\"%d\">%d</text>", x, y, text);
    }

    private String svgVLine(int x, int y1, int y2) {
        return svgLine(x, x, y1, y2);
    }

    private String svgHLine(int x1, int x2, int y) {
        return svgLine(x1, x2, y, y);
    }

    private String svgLine(int x1, int x2, int y1, int y2) {
        return String.format("<line x1=\"%d\" x2=\"%d\" y1=\"%d\" y2=\"%d\"/>", x1, x2, y1, y2);
    }

    private String generateLines() {
        return lines.stream()
                .map(lineData -> String.join("\n",
                        "<polyline style=\"stroke:" + lineData.getColor() + "\" class=\"curve\" points=\"",
                        IntStream.range(0, lineData.lineData.size())
                                .mapToObj(i -> axeX.getPosition(i) + "," + axeY.getPosition(lineData.lineData.get(i)))
                                .collect(Collectors.joining("\n")),
                        "\"/>"))
                .collect(Collectors.joining("\n"));
    }

    private String generateStyle() {
        return String.join("\n",
                "<style>",
                ".graph {",
                "    stroke:rgb(200,200,200);",
                "    stroke-width:1;",
                "}",
                ".curve {",
                "    fill:none;",
                "    stroke-width:3;",
                "    marker: url(#markerCircle);",
                "    stroke:black;",
                "}",
                "</style>");
    }

    public SvgGraph withLine(String label, List<Integer> values) {
        String lineColor = colors.get((colorIndex++) % colors.size());

        return withLine(new SvgLineData(label, values, lineColor));
    }

    private SvgGraph withLine(SvgLineData lineData) {

        lines.add(lineData);
        return this;
    }
}
