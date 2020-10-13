package com.adaptionsoft.games.svg;

public class SvgRect extends SvgElement<SvgRect> implements
        SvgInterfaceRect<SvgRect>,
        SvgInterfacePresentation<SvgRect> {

    public SvgRect() {
        super(SvgRect.class, "rect");
    }
}
