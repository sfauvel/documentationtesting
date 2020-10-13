package com.adaptionsoft.games.svg;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * https://www.w3.org/TR/SVG11/types.html
 */
public class SvgElement<T> implements SvgSetAttr<T> {
    protected final T myself;
    protected final String tag;
    protected final List<SvgElement> elements = new ArrayList<>();
    protected final Map<String, String> attributes = new HashMap<String, String>();
    private final Optional<String> boardIndex;
    private Optional<String> content = Optional.empty();

    public SvgElement(Class<T> selfType, String tag) {
        this.myself = selfType.cast(this);
        this.tag = tag;
        this.boardIndex = Optional.empty();
    }

    public SvgElement(Class<T> selfType, String tag, String boardIndex) {
        this.myself = selfType.cast(this);
        this.tag = tag;
        this.boardIndex = Optional.of(boardIndex);
    }

    public String buildId(int animationIndex) {
        return String.format("b%s_anim%d", boardIndex.orElseThrow(() -> new RuntimeException("No board index provided")), animationIndex);
    }

    public T setIdFromIndex(int animationIndex) {
        return setId(buildId(animationIndex));
    }

    public Optional<String> getId() {
        return Optional.ofNullable(attributes.get("id"));
    }

    public T setId(final String id) {
        return set("id", id);
    }

    public T set(final String key, final String value) {
        attributes.put(key, value);
        return myself;
    }

    public T setContent(final String value) {
        content = Optional.of(value);
        return myself;
    }
    public T add(final SvgElement element) {
        elements.add(element);
        return myself;
    }
    public T addAll(final List<SvgElement> elements) {
        this.elements.addAll(elements);
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
        if (elements.isEmpty() && !content.isPresent()) {
            return Optional.empty();
        }

        Function<String, String> indent = text -> Arrays.stream(text.split("\n"))
                .map(line -> String.format("  %s", line))
                .collect(Collectors.joining("\n"));

        return Optional.of(elements.stream()
                .map(SvgElement::toSvg)
                .map(indent)
                .map(text -> text + "\n")
                .collect(Collectors.joining()) +
                content.map(text -> "  " + text + "\n").orElse(""));
    }

}
