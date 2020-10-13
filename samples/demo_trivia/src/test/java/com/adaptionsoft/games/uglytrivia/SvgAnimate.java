package com.adaptionsoft.games.uglytrivia;

import javax.swing.text.html.Option;
import java.util.*;
import java.util.stream.Collectors;

class SvgSet extends SvgAnimation<SvgSet> {

    public SvgSet(String boardIndex) {
        super(SvgSet.class, "set", boardIndex);
    }
}

class SvgAnimate extends SvgAnimation<SvgAnimate> {

    public SvgAnimate(String boardIndex) {
        super(SvgAnimate.class, "animate", boardIndex);
    }
}

class SvgRect extends SvgElement<SvgRect> {

    public SvgRect() {
        super(SvgRect.class, "rect");
    }

    public SvgRect setX(String x) {
        return setKeyValue("x", x);
    }

    public SvgRect setY(String y) {
        return setKeyValue("y", y);
    }
    public SvgRect setWidth(String width) {
        return setKeyValue("width", width);
    }

    public SvgRect setHeight(String height) {
        return setKeyValue("height", height);
    }
    public SvgRect setOpacity(String opacity) {
        return setKeyValue("opacity", opacity);
    }

}

abstract class SvgElement<T> {
    protected final T myself;
    protected final String tag;
    protected final List<SvgElement> elements = new ArrayList<>();
    protected final Map<String, String> attributes = new HashMap<String, String>();

    public SvgElement(Class<T> selfType, String tag) {
        this.myself = selfType.cast(this);
        this.tag = tag;
    }

    protected T setKeyValue(String key, String value) {
        attributes.put(key, value);
        return myself;
    }

    public T add(SvgElement element) {
        elements.add(element);
        return myself;
    };

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
        return Optional.of(elements.stream()
                .map(SvgElement::toSvg)
                .map(svg -> String.format("  %s", svg))
                .collect(Collectors.joining()));
    }

}

abstract class SvgAnimation<T> extends SvgElement<T> {

    private final String boardIndex;
    private Optional<String> id = Optional.empty();
    private Optional<String> xlink = Optional.empty();
    private String begin = null;
    private String attribute = null;
    private Optional<String> from = Optional.empty();
    private String to = null;
    private Optional<String> duration = Optional.empty();

    public SvgAnimation(Class<T> selfType, String tag, String boardIndex) {
        super(selfType, tag);
        this.boardIndex = boardIndex;

        attributes.put("repeatCount", "1");
        attributes.put("fill", "freeze");
    }

    public String getAnimId(int animationIndex) {
        return String.format("b%s_anim%d", boardIndex, animationIndex);
    }

    public Optional<String> getId() {
        return id;
//        return Optional.ofNullable(attributes.get("id"));
    }

    public T setId(String id) {
        this.id = Optional.of(id);
        return setKeyValue("id", id);
    }

    public T setXlink(String xlink) {
        return setKeyValue("xlink:href", "#" + xlink);
    }

    public T setBegin(String begin) {
        return setKeyValue("begin", begin);
    }

    public T setAttribute(String attribute) {
        return setKeyValue("attributeName", attribute);
    }

    public T setFrom(String from) {
        return setKeyValue("from", from);
    }

    public T setTo(int to) {
        return setTo(Integer.toString(to));
    }

    public T setTo(String to) {
        return setKeyValue("to", to);
    }

    public T setDuration(String duration) {
        this.duration = Optional.of(duration);
        attributes.put("dur", duration);
        return setKeyValue("dur", duration);
    }

    public T setAnimId(int animationIndex) {
        return setId(getAnimId(animationIndex));
    }

    public T setBeginRelativeTo(int animationIndex, String event) {
        return setBegin(String.format("%s.%s", getAnimId(animationIndex), event));
    }
}
