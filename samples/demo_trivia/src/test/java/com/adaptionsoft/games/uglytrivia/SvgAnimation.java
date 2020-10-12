package com.adaptionsoft.games.uglytrivia;

import java.util.Optional;

class SvgAnimation {

    private final String boardIndex;
    private Optional<String> id = Optional.empty();
    private Optional<String> xlink = Optional.empty();
    private String begin = null;
    private String attribute = null;
    private Optional<String> from = Optional.empty();
    private String to = null;
    private String duration = null;

    public SvgAnimation(String boardIndex) {
        this.boardIndex = boardIndex;
    }

    public String getAnimId(int animationIndex) {
        return String.format("b%s_anim%d", boardIndex, animationIndex);
    }

    public Optional<String> getId() {
        return id;
    }

    public SvgAnimation setId(String id) {
        this.id = Optional.of(id);
        return this;
    }

    public SvgAnimation setXlink(String xlink) {
        this.xlink = Optional.of(xlink);
        return this;
    }

    public SvgAnimation setBegin(String begin) {
        this.begin = begin;
        return this;
    }

    public SvgAnimation setAttribute(String attribute) {
        this.attribute = attribute;
        return this;
    }

    public SvgAnimation setFrom(String from) {
        this.from = Optional.of(from);
        return this;
    }

    public SvgAnimation setTo(String to) {
        this.to = to;
        return this;
    }

    public SvgAnimation setDuration(String duration) {
        this.duration = duration;
        return this;
    }

    public String toSvg() {
        return "<animate" +
                id.map(value -> String.format(" id=\"%s\"", value)).orElse("") +
                xlink.map(value -> String.format(" xlink:href=\"#%s\"", value)).orElse("") +
                String.format(" begin=\"%s\"", begin) +
                String.format(" attributeName=\"%s\"", attribute) +
                from.map(value -> String.format(" from=\"%s\"", value)).orElse("") +
                String.format(" to=\"%s\"", to) +
                String.format(" dur=\"%s\"", duration) +
                " repeatCount=\"1\" fill=\"freeze\"/>\n";
    }

    public SvgAnimation setAnimId(int animationIndex) {
        return setId(getAnimId(animationIndex));
    }

    public SvgAnimation setBeginRelativeTo(int animationIndex, String event) {
        return setBegin(String.format("%s.%s", getAnimId(animationIndex), event));
    }
}
