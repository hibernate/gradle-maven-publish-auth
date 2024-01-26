/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Paul Scholz
 */
public final class ValueProcessorRegex implements ValueProcessor {

    /**
     * Singleton access
     */
    public static final ValueProcessorRegex INSTANCE = new ValueProcessorRegex();

    private static final Pattern PATTERN = Pattern.compile("\\$\\{([a-zA-Z0-9-._]+)}");
    private final PropertyMap systemPropertiesAndEnv;

    private ValueProcessorRegex() {
        systemPropertiesAndEnv = SimplePropertyMap.INSTANCE;
    }

    @Override
    public String processValue(String value) {
        if (value == null) {
            return null;
        }
        final Matcher matcher = PATTERN.matcher(value);
        StringBuilder s = new StringBuilder();
        while (matcher.find()) {
            final String group = matcher.group(1);
            matcher.appendReplacement(s, systemPropertiesAndEnv.get(group).map(Matcher::quoteReplacement).orElse(Matcher.quoteReplacement(matcher.group())));
        }
        if (s.isEmpty()) {
            return value;
        } else {
            return s.toString();
        }
    }
}
