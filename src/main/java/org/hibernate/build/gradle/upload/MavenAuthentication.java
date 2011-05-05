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

import java.lang.reflect.Method;

/**
 * A wrapper around the {@link org.apache.maven.artifact.ant.Authentication} class from the maven ant tasks due to
 * some change in Gradle causing classloader problems.
 *
 * @author Steve Ebersole
 */
public class MavenAuthentication {
	private final Object delegate;

	public MavenAuthentication() {
		try {
			this.delegate = getAuthClass().newInstance();
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to instantiate " + AUTH_CLASS_NAME, e );
		}
	}

	public void setUserName(String username) {
		try {
			getUserNameSetter().invoke( delegate, username );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke setUserName method", e );
		}
	}

	public void setPassword(String password) {
		try {
			getPasswordSetter().invoke( delegate, password );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke setPassword method", e );
		}
	}

	public void setPrivateKey(String privateKey) {
		try {
			getPrivateKeySetter().invoke( delegate, privateKey );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke setPrivateKey method", e );
		}
	}

	public void setPassphrase(String passphrase) {
		try {
			getPassphraseSetter().invoke( delegate, passphrase );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke setPassphrase method", e );
		}
	}

	Object getDelegate() {
		return delegate;
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private static Method USER_NAME_SETTER_METHOD;

	private static Method getUserNameSetter() {
		if ( USER_NAME_SETTER_METHOD == null ) {
			USER_NAME_SETTER_METHOD = locateUserNameSetter();
		}
		return USER_NAME_SETTER_METHOD;
	}

	private static Method locateUserNameSetter() {
		try {
			return getAuthClass().getMethod( "setUserName", String.class );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate setUserName method", e );
		}
	}

	private static Method PASSWORD_SETTER_METHOD;

	private static Method getPasswordSetter() {
		if ( PASSWORD_SETTER_METHOD == null ) {
			PASSWORD_SETTER_METHOD = locatePasswordSetter();
		}
		return PASSWORD_SETTER_METHOD;
	}

	private static Method locatePasswordSetter() {
		try {
			return getAuthClass().getMethod( "setPassword", String.class );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate setPassword method", e );
		}
	}

	private static Method PRIVATE_KEY_SETTER_METHOD;

	private static Method getPrivateKeySetter() {
		if ( PRIVATE_KEY_SETTER_METHOD == null ) {
			PRIVATE_KEY_SETTER_METHOD = locatePrivateKeySetter();
		}
		return PRIVATE_KEY_SETTER_METHOD;
	}

	private static Method locatePrivateKeySetter() {
		try {
			return getAuthClass().getMethod( "setPrivateKey", String.class );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate setPrivateKey method", e );
		}
	}

	private static Method PASSPHRASE_SETTER_METHOD;

	private static Method getPassphraseSetter() {
		if ( PASSPHRASE_SETTER_METHOD == null ) {
			PASSPHRASE_SETTER_METHOD = locatePassphraseSetter();
		}
		return PASSPHRASE_SETTER_METHOD;
	}

	private static Method locatePassphraseSetter() {
		try {
			return getAuthClass().getMethod( "setPassphrase", String.class );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate setPassphrase method", e );
		}
	}

	private static final String AUTH_CLASS_NAME = "org.apache.maven.artifact.ant.Authentication";
	private static Class AUTH_CLASS;

	public static Class getAuthClass() {
		if ( AUTH_CLASS == null ) {
			AUTH_CLASS = locateAuthClass();
		}
		return AUTH_CLASS;
	}

	private static Class locateAuthClass() {
		try {
			return Class.forName( AUTH_CLASS_NAME );
		}
		catch (ClassNotFoundException e) {
			throw new ReflectionException( "Unable to locate class [" + AUTH_CLASS_NAME + "]", e );
		}
	}
}
