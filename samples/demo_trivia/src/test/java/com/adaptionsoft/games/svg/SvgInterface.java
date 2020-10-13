package com.adaptionsoft.games.svg;

interface SvgSetAttr<T> {
    T set(String key, String value);
}

/**
 * https://www.w3.org/TR/SVG11/types.html#InterfaceSVGRect
 */
interface SvgInterfaceRect<T> extends SvgSetAttr<T> {
    default T setX(int value) {
        return setX(Integer.toString(value));
    }

    default T setX(String value) {
        return set("x", value);
    }

    default T setY(int value) {
        return setY(Integer.toString(value));
    }

    default T setY(String value) {
        return set("y", value);
    }

    default T setWidth(int value) {
        return setWidth(Integer.toString(value));
    }

    default T setWidth(String value) {
        return set("width", value);
    }

    default T setHeight(int value) {
        return setHeight(Integer.toString(value));
    }

    default T setHeight(String value) {
        return set("height", value);
    }

    default T setRect(int x, int y, int width, int height) {
        setX(x);
        setY(y);
        setWidth(width);
        return setHeight(height);
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#TargetAttributes
 * ‘attributeType’, ‘attributeName’
 */
interface SvgInterfaceTargetAttribute<T> extends SvgSetAttr<T> {
    default T setAttributeName(String value) {
        return set("attributeName", value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/intro.html#TermXLinkAttributes
 * ‘xlink:href’, ‘xlink:show’, ‘xlink:actuate’, ‘xlink:type’, ‘xlink:role’, ‘xlink:arcrole’, ‘xlink:title’
 */
interface SvgInterfaceXLink<T> extends SvgSetAttr<T> {
    default T setXLinkHref(String value) {
        return set("xlink:href", "#" + value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#ValueAttributes
 * ‘calcMode’, ‘values’, ‘keyTimes’, ‘keySplines’, ‘from’, ‘to’, ‘by’
 */
interface SvgInterfaceAnimationValue<T> extends SvgSetAttr<T> {
    default T setFrom(int value) {
        return setFrom(Integer.toString(value));
    }

    default T setFrom(String value) {
        return set("from", value);
    }

    default T setTo(int value) {
        return setTo(Integer.toString(value));
    }

    default T setTo(String value) {
        return set("to", value);
    }
}

/**
 * https://www.w3.org/TR/SVG11/animate.html#TimingAttributes
 * ‘begin’, ‘dur’, ‘end’, ‘min’, ‘max’, ‘restart’, ‘repeatCount’, ‘repeatDur’, ‘fill’
 *
 * @param <T>
 */
interface SvgInterfaceAnimationTiming<T> extends SvgSetAttr<T> {
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
 * ‘alignment-baseline’, ‘baseline-shift’, ‘clip’, ‘clip-path’, ‘clip-rule’, ‘color’, ‘color-interpolation’, ‘color-interpolation-filters’,
 * ‘color-profile’, ‘color-rendering’, ‘cursor’, ‘direction’, ‘display’, ‘dominant-baseline’, ‘enable-background’, ‘fill’, ‘fill-opacity’,
 * ‘fill-rule’, ‘filter’, ‘flood-color’, ‘flood-opacity’, ‘font-family’, ‘font-size’, ‘font-size-adjust’, ‘font-stretch’, ‘font-style’,
 * ‘font-variant’, ‘font-weight’, ‘glyph-orientation-horizontal’, ‘glyph-orientation-vertical’, ‘image-rendering’, ‘kerning’, ‘letter-spacing’,
 * ‘lighting-color’, ‘marker-end’, ‘marker-mid’, ‘marker-start’, ‘mask’, ‘opacity’, ‘overflow’, ‘pointer-events’, ‘shape-rendering’, ‘
 * stop-color’, ‘stop-opacity’, ‘stroke’, ‘stroke-dasharray’, ‘stroke-dashoffset’, ‘stroke-linecap’, ‘stroke-linejoin’, ‘stroke-miterlimit’,
 * ‘stroke-opacity’, ‘stroke-width’, ‘text-anchor’, ‘text-decoration’, ‘text-rendering’, ‘unicode-bidi’, ‘visibility’, ‘word-spacing’, ‘writing-mode’
 */
interface SvgInterfacePresentation<T> extends SvgSetAttr<T> {

    default T setColor(String value) {
        return set("color", value);
    }

    default T setDominantBaseline(String value) {
        return set("dominant-baseline", value);
    }

    default T setFill(String value) {
        return set("fill", value);
    }

    default T setFontFamily(String value) {
        return set("font-family", value);
    }

    default T setFontSize(String value) {
        return set("font-size", value);
    }

    default T setOpacity(String value) {
        return set("opacity", value);
    }

    default T setStroke(String value) {
        return set("stroke", value);
    }

    default T setStrokeWidth(String value) {
        return set("stroke-width", value);
    }

    default T setTextAnchor(String value) {
        return set("text-anchor", value);
    }
}
