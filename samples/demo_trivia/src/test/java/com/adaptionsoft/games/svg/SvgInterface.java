package com.adaptionsoft.games.svg;

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
