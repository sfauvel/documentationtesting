package com.adaptionsoft.games.svg;

/**
 * https://www.w3.org/TR/SVG11/text.html#TextElement
 * ‘class’, ‘style’, ‘externalResourcesRequired’, ‘transform’, ‘lengthAdjust’, ‘x’, ‘y’, ‘dx’, ‘dy’, ‘rotate’, ‘textLength’
 */
public class SvgText extends SvgElement<SvgText> implements
        SvgInterfacePresentation<SvgText> {

    public SvgText(String boardIndex) {
        super(SvgText.class, "text", boardIndex);
    }

    public SvgText setX(String value) {
        return set("x", value);
    }

    public SvgText setY(String value) {
        return set("y", value);
    }
}
