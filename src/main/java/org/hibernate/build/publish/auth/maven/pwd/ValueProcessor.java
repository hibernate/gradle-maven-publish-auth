/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import groovy.text.GStringTemplateEngine;
import groovy.text.SimpleTemplateEngine;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ruslan Mikhalev
 */
public final class ValueProcessor {

	public static final Logger log = LoggerFactory.getLogger( MethodHandles.lookup().lookupClass() );

	private final GStringTemplateEngine engine;
	private final PropertyMap systemPropertiesAndEnv;

	@SuppressWarnings("unchecked")
	private ValueProcessor() {
		this.systemPropertiesAndEnv = new PropertyMap( ( Map )System.getProperties() );
		systemPropertiesAndEnv.put( "env", System.getenv() );

		this.engine = new GStringTemplateEngine();
	}
	/**
	 * Singleton access
	 */
	public static final ValueProcessor INSTANCE = new ValueProcessor();

	public String processValue(String value) {
	    if ( value == null ) {
	        return null;
        }
		try {
			return engine
					.createTemplate( value )
					.make( systemPropertiesAndEnv )
					.toString();
		} catch (IOException | ClassNotFoundException e) {
			throw new AssertionError( "It should never happen", e );
		}
	}
}
