/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.util;

/**
 * @author Steve Ebersole
 */
public class PathHelper {
	private PathHelper() {
	}

	public static String normalizePath(String path) {
		if ( path.startsWith( "~" ) ) {
			path = System.getProperty( "user.home" ) + path.substring( 1 );
		}
		return path;
	}
}
