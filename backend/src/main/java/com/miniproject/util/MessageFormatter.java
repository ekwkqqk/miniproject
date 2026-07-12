package com.miniproject.util;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 메시지 템플릿 포맷터.
 * - 위치 파라미터: {0}, {1}, ...
 * - 이름 파라미터: {name}, {count}, ...
 */
public final class MessageFormatter {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");

    private MessageFormatter() {
    }

    public static String format(String template, Object... args) {
        if (template == null) {
            return null;
        }
        if (args == null || args.length == 0) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            String replacement;
            if (key.matches("\\d+")) {
                int index = Integer.parseInt(key);
                replacement = index >= 0 && index < args.length && args[index] != null
                        ? Matcher.quoteReplacement(String.valueOf(args[index]))
                        : Matcher.quoteReplacement(matcher.group());
            } else {
                replacement = Matcher.quoteReplacement(matcher.group());
            }
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static String format(String template, Map<String, ?> params) {
        if (template == null) {
            return null;
        }
        if (params == null || params.isEmpty()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER.matcher(template);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String key = matcher.group(1);
            Object value = params.get(key);
            if (value == null && key.matches("\\d+")) {
                // allow numeric keys in map too
                value = params.get(key);
            }
            String replacement = value != null
                    ? Matcher.quoteReplacement(String.valueOf(value))
                    : Matcher.quoteReplacement(matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}
