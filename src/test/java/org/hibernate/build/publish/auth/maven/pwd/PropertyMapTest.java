package org.hibernate.build.publish.auth.maven.pwd;

import org.junit.Test;

import java.util.HashMap;

import static org.junit.Assert.*;

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