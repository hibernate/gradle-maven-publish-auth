/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2010, Red Hat Inc. or third-party contributors as
 * indicated by the @author tags or express copyright attribution
 * statements applied by the authors.  All third-party contributions are
 * distributed under license by Red Hat Inc.
 *
 * This copyrighted material is made available to anyone wishing to use, modify,
 * copy, or redistribute it subject to the terms and conditions of the GNU
 * Lesser General Public License, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this distribution; if not, write to:
 * Free Software Foundation, Inc.
 * 51 Franklin Street, Fifth Floor
 * Boston, MA  02110-1301  USA
 */
package org.hibernate.build.gradle.upload;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A wrapper around the {@link org.apache.maven.artifact.ant.RemoteRepository} class from the maven ant tasks due to
 * some change in Gradle causing classloader problems.
 *
 * @author Steve Ebersole
 */
public class MavenRepository {
	private final Object delegate;

	public MavenRepository(Object delegate) {
		this.delegate = delegate;
	}

	public String getId() {
		try {
			return (String) idGetterMethod().invoke( delegate );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke getId method", e );
		}
	}

	public String getUrl() {
		try {
			return (String) urlGetterMethod().invoke( delegate );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke getUrl method", e );
		}
	}

	public void addAuthentication(MavenAuthentication authentication) {
		try {
			authenticationAdderMethod().invoke( delegate, authentication.getDelegate() );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke addAuthentication method", e );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private static Method ID_GETTER_METHOD;

	public Method idGetterMethod() {
		if ( ID_GETTER_METHOD == null ) {
			ID_GETTER_METHOD = locateIdGetterMethod();
		}
		return ID_GETTER_METHOD;
	}

	private Method locateIdGetterMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "getId" );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate getId method", e );
		}
	}

	private static Method URL_GETTER_METHOD;

	public Method urlGetterMethod() {
		if ( URL_GETTER_METHOD == null ) {
			URL_GETTER_METHOD = locateUrlGetterMethod();
		}
		return URL_GETTER_METHOD;
	}

	private Method locateUrlGetterMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "getUrl" );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate getUrl method", e );
		}
	}

	private static Method ADD_AUTHENTICATION_METHOD;

	private Method authenticationAdderMethod() {
		if ( ADD_AUTHENTICATION_METHOD == null ) {
			ADD_AUTHENTICATION_METHOD = locateAuthenticationAdderMethod();
		}
		return ADD_AUTHENTICATION_METHOD;
	}

	private Method locateAuthenticationAdderMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "addAuthentication", MavenAuthentication.getAuthClass() );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate addAuthentication method", e );
		}
	}

	private static final String REMOTE_REPO_CLASS_NAME = "org.apache.maven.artifact.ant.RemoteRepository";
	private static Class REMOTE_REPO_CLASS;

	public static Class getRemoteRepositoryClass() {
		if ( REMOTE_REPO_CLASS == null ) {
			REMOTE_REPO_CLASS = locateRemoteRepositoryClass();
		}
		return REMOTE_REPO_CLASS;
	}

	private static Class locateRemoteRepositoryClass() {
		try {
			return Class.forName( REMOTE_REPO_CLASS_NAME );
		}
		catch (ClassNotFoundException e) {
			throw new ReflectionException( "Unable to locate class [" + REMOTE_REPO_CLASS_NAME + "]", e );
		}
	}
}
