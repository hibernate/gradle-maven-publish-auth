/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

/**
 * @author Ruslan Mikhalev
 */
public class PropertyMapTest {

    @Test
    public void testPropertyMapBasic() {
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put( "user.name", "foo" );
        PropertyMap propertyMap = new PropertyMap( hashMap );
        assertNotNull( propertyMap.get( "user" ) );
        assertEquals( "foo", ( ( PropertyMap )propertyMap.get( "user" ) ).get( "name" ) );
    }

}