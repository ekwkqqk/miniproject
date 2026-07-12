package com.miniproject.mail.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public final class MailTemplateRenderer {

    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([^{}]+)}");

    private MailTemplateRenderer() {
    }

    public static String render(String template, Map<String, ?> params) {
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
            String replacement = value != null
                    ? Matcher.quoteReplacement(String.valueOf(value))
                    : Matcher.quoteReplacement(matcher.group());
            matcher.appendReplacement(sb, replacement);
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    public static List<String> parseAddresses(String raw) {
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split("[,;]"))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public static String joinAddresses(List<String> addresses) {
        if (addresses == null || addresses.isEmpty()) {
            return "";
        }
        return addresses.stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(", "));
    }
}
