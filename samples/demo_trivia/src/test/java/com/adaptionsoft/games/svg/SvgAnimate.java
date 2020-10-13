package com.adaptionsoft.games.svg;

/**
 * https://www.w3.org/TR/SVG11/animate.html#AnimateElement
 */
public class SvgAnimate extends SvgAnimateElement<SvgAnimate> {

    public SvgAnimate(String boardIndex) {
        super(SvgAnimate.class, "animate", boardIndex);
    }
}

