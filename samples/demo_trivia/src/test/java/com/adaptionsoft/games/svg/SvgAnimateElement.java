package com.adaptionsoft.games.svg;

/**
 * https://www.w3.org/TR/SVG11/animate.html#AnimateElement
 *
 * @param <T>
 */
public class SvgAnimateElement<T> extends SvgElement<T> implements
        SvgInterfaceAnimationTiming<T>,
        SvgInterfaceAnimationValue<T>,
        SvgInterfaceXLink<T>,
        SvgInterfaceTargetAttribute<T> {

    public SvgAnimateElement(Class<T> selfType, String tag, String boardIndex) {
        super(selfType, tag, boardIndex);

        setRepeatCount("1");
        setFill("freeze");
    }

    public T setBeginRelativeTo(int animationIndex, String event) {
        return setBegin(String.format("%s.%s", buildId(animationIndex), event));
    }

}
