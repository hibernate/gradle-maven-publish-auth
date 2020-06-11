/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import java.util.Optional;

/**
 * Contract of collecting properties to be replaced in settings.xml.
 */
public interface PropertyMap {

    /**
     * Retrieve value to a key
     *
     * @param key property key
     * @return Optional is empty if key could not be found
     * @see Optional#isPresent()
     * @see Optional#get()
     */
    Optional<String> get(String key);

}
