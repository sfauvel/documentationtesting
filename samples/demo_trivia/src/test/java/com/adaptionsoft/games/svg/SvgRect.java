package com.adaptionsoft.games.svg;

public class SvgRect extends SvgElement<SvgRect> implements
        SvgXY<SvgRect>,
        SvgWithHeight<SvgRect>,
        SvgOpacity<SvgRect> {

    public SvgRect() {
        super(SvgRect.class, "rect");
    }
}
