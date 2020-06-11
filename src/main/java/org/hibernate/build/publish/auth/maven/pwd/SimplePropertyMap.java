/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import java.util.Optional;

/**
 * @author Paul Scholz
 */
public class SimplePropertyMap implements PropertyMap {

    public static final SimplePropertyMap INSTANCE = new SimplePropertyMap();

    @Override
    public Optional<String> get(String key) {
        if (key.startsWith("env")) {
            final String s = key.replaceFirst("env\\.", "");
            return Optional.ofNullable(System.getenv(s));
        } else {
            return Optional.ofNullable(System.getProperty(key));
        }
    }

}
