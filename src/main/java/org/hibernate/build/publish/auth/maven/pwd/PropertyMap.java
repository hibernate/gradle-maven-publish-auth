/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.invoke.MethodHandles;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruslan Mikhalev
 */
public class PropertyMap extends HashMap<String, Object> {

    public static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

    public PropertyMap(Map<String, Object> m) {
        super(m);
    }

    @Override
    public Object get(Object key) {
        if (key == null || super.get( key ) != null) {
            return super.get( key );
        }
        HashMap<String, Object> properties = new HashMap<>();
        for ( Entry<String, Object> entry: entrySet() ) {
            if ( entry.getKey() != null && entry.getKey().startsWith( key + "." ) ) {
                properties.put( entry.getKey().substring( key.toString().length() + 1 ), entry.getValue() );
            }
        }
        return properties.size() > 0 ? new PropertyMap( properties ) : null;
    }


}
