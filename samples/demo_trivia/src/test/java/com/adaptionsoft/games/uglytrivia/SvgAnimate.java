package com.adaptionsoft.games.uglytrivia;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

class SvgSet extends SvgAnimateElement<SvgSet> {

    public SvgSet(String boardIndex) {
        super(SvgSet.class, "set", boardIndex);
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#AnimateElement
 */
class SvgAnimate extends SvgAnimateElement<SvgAnimate> {

    public SvgAnimate(String boardIndex) {
        super(SvgAnimate.class, "animate", boardIndex);
    }
}


class SvgRect extends SvgElementWrapper<SvgRect> implements
        SvgXY<SvgRect>,
        SvgWithHeight<SvgRect>,
        SvgOpacity<SvgRect> {

    public SvgRect() {
        super(SvgRect.class, new SvgElement(SvgElement.class, "rect"));
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#AnimateElement
 * @param <T>
 */
class SvgAnimateElement<T> extends SvgElementWrapper<T> implements
        SvgAnimationTiming<T>,
        SvgAnimationValue<T>,
        SvgXLink<T>,
        SvgTargetAttribute<T> {

    private final String boardIndex;
    public SvgAnimateElement(Class<T> selfType, String tag, String boardIndex) {
        super(selfType, new SvgElement(SvgElement.class, tag));
        this.boardIndex = boardIndex;

        set("repeatCount", "1");
        set("fill", "freeze");
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

interface SvgSetAttr<T> {
    T set(String key, String value);
}

interface SvgXY<T> extends SvgSetAttr<T> {
    default T setX(String value) {
        return set("x", value);
    }

    default T setY(String value) {
        return set("y", value);
    }
}

interface SvgWithHeight<T> extends SvgSetAttr<T> {
    default T setWidth(String value) {
        return set("width", value);
    }

    default T setHeight(String value) {
        return set("height", value);
    }
}

interface SvgOpacity<T> extends SvgSetAttr<T> {
    default T setOpacity(String value) {
        return set("opacity", value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#TargetAttributes
 * ‘attributeType’, ‘attributeName’
 */
interface SvgTargetAttribute<T> extends SvgSetAttr<T> {
    default T setAttributeName(String value) {
        return set("attributeName", value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/intro.html#TermXLinkAttributes
 * ‘xlink:href’, ‘xlink:show’, ‘xlink:actuate’, ‘xlink:type’, ‘xlink:role’, ‘xlink:arcrole’, ‘xlink:title’
 */
interface SvgXLink<T> extends SvgSetAttr<T> {
    default T setXLinkHref(String value) {
        return set("xlink:href", "#"+value);
    }
 }

/**
 * https://www.w3.org/TR/SVG11/animate.html#ValueAttributes
 ‘calcMode’, ‘values’, ‘keyTimes’, ‘keySplines’, ‘from’, ‘to’, ‘by’
 */
interface SvgAnimationValue<T> extends SvgSetAttr<T> {
    default T setFrom(String value) {
        return set("from", value);
    }
    default T setTo(String value) {
        return set("to", value);
    }

}

/**
 * https://www.w3.org/TR/SVG11/animate.html#TimingAttributes
 * ‘begin’, ‘dur’, ‘end’, ‘min’, ‘max’, ‘restart’, ‘repeatCount’, ‘repeatDur’, ‘fill’
 * @param <T>
 */
interface SvgAnimationTiming<T> extends SvgSetAttr<T> {
    default T setBegin(String value) {
        return set("begin", value);
    }
    default T setDuration(String value) {
        return set("dur", value);
    }
    default T setRepeatCount(String value) {
        return set("repeatCount", value);
    }
    default T setFill(String value) {
        return set("fill", value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/intro.html#TermPresentationAttribute
 * ‘alignment-baseline’, ‘baseline-shift’, ‘clip’, ‘clip-path’, ‘clip-rule’, ‘color’, ‘color-interpolation’, ‘color-interpolation-filters’, ‘color-profile’, ‘color-rendering’, ‘cursor’, ‘direction’, ‘display’, ‘dominant-baseline’, ‘enable-background’, ‘fill’, ‘fill-opacity’, ‘fill-rule’, ‘filter’, ‘flood-color’, ‘flood-opacity’, ‘font-family’, ‘font-size’, ‘font-size-adjust’, ‘font-stretch’, ‘font-style’, ‘font-variant’, ‘font-weight’, ‘glyph-orientation-horizontal’, ‘glyph-orientation-vertical’, ‘image-rendering’, ‘kerning’, ‘letter-spacing’, ‘lighting-color’, ‘marker-end’, ‘marker-mid’, ‘marker-start’, ‘mask’, ‘opacity’, ‘overflow’, ‘pointer-events’, ‘shape-rendering’, ‘stop-color’, ‘stop-opacity’, ‘stroke’, ‘stroke-dasharray’, ‘stroke-dashoffset’, ‘stroke-linecap’, ‘stroke-linejoin’, ‘stroke-miterlimit’, ‘stroke-opacity’, ‘stroke-width’, ‘text-anchor’, ‘text-decoration’, ‘text-rendering’, ‘unicode-bidi’, ‘visibility’, ‘word-spacing’, ‘writing-mode’
 */
interface SvgPresentation<T> extends SvgSetAttr<T> {
    default T setOpacity(String value) {
        return set("opacity", value);
    }
    default T setColor(String value) {
        return set("color", value);
    }
}


class SvgElementWrapper<T> {
    protected final T myself;
    protected final SvgElement delegate;

    public SvgElementWrapper(Class<T> selfType, SvgElement delegate) {
        this.myself = selfType.cast(this);
        this.delegate = delegate;
    }

    public T set(String key, String value) {
        delegate.setKeyValue(key, value);
        return myself;
    }

    public T add(SvgElement svgElement) {
        delegate.add(svgElement);
        return myself;
    }

    public T add(SvgElementWrapper element) {
        return add(element.delegate);
    }

    public String toSvg() {
        return delegate.toSvg();
    }

    public T setId(String id) {
        delegate.setId(id);
        return myself;
    }
    public Optional<String> getId() {
        return delegate.getId();
    }
}

/**
 * https://www.w3.org/TR/SVG11/types.html
 */
class SvgElement<T> {
    protected final T myself;
    protected final String tag;
    protected final List<SvgElement> elements = new ArrayList<>();
    protected final Map<String, String> attributes = new HashMap<String, String>();

    public SvgElement(Class<T> selfType, String tag) {
        this.myself = selfType.cast(this);
        this.tag = tag;
    }

    public Optional<String> getId() {
        return Optional.ofNullable(attributes.get("id"));
    }

    public T setId(String id) {
        return setKeyValue("id", id);
    }

    protected T setKeyValue(String key, String value) {
        attributes.put(key, value);
        return myself;
    }

    public T add(SvgElement element) {
        elements.add(element);
        return myself;
    }

    public String toSvg() {
        Optional<String> content = getContent();
        Optional<String> attributes = getAttributes();
        return String.format("<%s%s%s\n",
                tag,
                attributes.map(a -> " " + a).orElse(""),
                content.map(c -> ">\n" + c + "</" + tag + ">").orElse("/>"));
    }

    protected Optional<String> getAttributes() {
        if (attributes.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(attributes.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(e -> String.format("%s=\"%s\"", e.getKey(), e.getValue()))
                .collect(Collectors.joining(" ")));
    }

    protected Optional<String> getContent() {
        if (elements.isEmpty()) {
            return Optional.empty();
        }

        Function<String, String> indent = text -> Arrays.stream(text.split("\n"))
                .map(line -> String.format("  %s", line))
                .collect(Collectors.joining("\n"));

        return Optional.of(elements.stream()
                .map(SvgElement::toSvg)
                .map(indent)
                .map(text -> text + "\n")
                .collect(Collectors.joining()));
    }

}

