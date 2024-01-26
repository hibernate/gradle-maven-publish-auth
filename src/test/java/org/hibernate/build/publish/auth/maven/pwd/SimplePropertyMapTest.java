/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables;
import uk.org.webcompere.systemstubs.jupiter.SystemStub;
import uk.org.webcompere.systemstubs.jupiter.SystemStubsExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SystemStubsExtension.class)
public class SimplePropertyMapTest {

    @SystemStub
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private final String NOT_EXISTENT_KEY = "not.existent";
//    @Rule
//    public final ClearSystemProperties restoreSystemProperties = new ClearSystemProperties(NOT_EXISTENT_KEY);

    @Test
    public void systemPropertyPresent() {
        final String key = "user.name";
        final String value = "scholzi100";
        System.setProperty(key, value);
        final PropertyMap propertyMap = new SimplePropertyMap();
        final Optional<String> optional = propertyMap.get(key);
        assertTrue(optional.isPresent());
        assertEquals(value, optional.get());
    }

    @Test
    public void systemPropertyNotPresent() {
        final PropertyMap propertyMap = new SimplePropertyMap();
        final Optional<String> optional = propertyMap.get(NOT_EXISTENT_KEY);
        Assertions.assertFalse(optional.isPresent());
    }

    @Test
    public void envPropertyPresent() {
        final String key = "HOME_MAVEN";
        final String value = "/usr/share/";
        environmentVariables.set(key, value);
        final PropertyMap propertyMap = new SimplePropertyMap();
        final Optional<String> optional = propertyMap.get("env." + key);
        assertTrue(optional.isPresent());
        assertEquals(value, optional.get());
    }

    @Test
    public void envPropertyNotPresent() {
        final String key = "HOME_MAVEN";
        //environmentVariables.clear(key);
        final PropertyMap propertyMap = new SimplePropertyMap();
        final Optional<String> optional = propertyMap.get("env." + key);
        Assertions.assertFalse(optional.isPresent());
    }

}
