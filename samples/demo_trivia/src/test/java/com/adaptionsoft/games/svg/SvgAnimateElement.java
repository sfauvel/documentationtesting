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

    private final String boardIndex;

    public SvgAnimateElement(Class<T> selfType, String tag, String boardIndex) {
        super(selfType, tag);
        this.boardIndex = boardIndex;

        setRepeatCount("1");
        setFill("freeze");
    }

    public String getAnimId(int animationIndex) {
        return String.format("b%s_anim%d", boardIndex, animationIndex);
    }

    public T setAnimId(int animationIndex) {
        return setId(getAnimId(animationIndex));
    }

    public T setBeginRelativeTo(int animationIndex, String event) {
        return setBegin(String.format("%s.%s", getAnimId(animationIndex), event));
    }

}
