/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

/**
 * @author Ruslan Mikhalev
 * @author Paul Scholz
 */
public interface ValueProcessor {

    /**
     * Processes a string possibly containing properties, those will be placed. <br>
     * Properties are defined as by <a href="https://maven.apache.org/settings.html#quick-overview">Maven</a>.
     *
     * @param value string w/o properties
     * @return null if value is null
     */
    String processValue(String value);

}
