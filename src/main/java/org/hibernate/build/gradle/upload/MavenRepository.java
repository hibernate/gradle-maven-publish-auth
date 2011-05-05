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
 * A wrapper around the {@link org.apache.maven.artifact.ant.RemoteRepository} class from the maven ant tasks due to
 * some change in Gradle causing classloader problems.
 *
 * @author Steve Ebersole
 */
public class MavenRepository {
	private final Object delegate;
    private final ClassLoader classLoader;

    public MavenRepository(Object delegate, ClassLoader classLoader) {
		this.delegate = delegate;
        this.classLoader = classLoader;
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
			authenticationAdderMethod().invoke( delegate, getDelegate(authentication) );
		}
		catch (Exception e) {
			throw new ReflectionException( "Unable to invoke addAuthentication method", e );
		}
	}


	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    private Object getDelegate(MavenAuthentication authentication) {
        Object delegate;
        try {
            delegate = getAuthClass().newInstance();
        }
        catch (Exception e) {
            throw new ReflectionException( "Unable to instantiate " + AUTH_CLASS_NAME, e );
        }
        setUserName(authentication.getUserName(), delegate);
        setPassword(authentication.getPassword(), delegate);
        setPrivateKey(authentication.getPrivateKey(), delegate);
        setPassphrase(authentication.getPassphrase(), delegate);
        return delegate;
    }

    private void setUserName(String username, Object delegate) {
        try {
            getUserNameSetter().invoke( delegate, username );
        }
        catch (Exception e) {
            throw new ReflectionException( "Unable to invoke setUserName method", e );
        }
    }

    private void setPassword(String password, Object delegate) {
        try {
            getPasswordSetter().invoke( delegate, password );
        }
        catch (Exception e) {
            throw new ReflectionException( "Unable to invoke setPassword method", e );
        }
    }

    private void setPrivateKey(String privateKey, Object delegate) {
        try {
            getPrivateKeySetter().invoke( delegate, privateKey );
        }
        catch (Exception e) {
            throw new ReflectionException( "Unable to invoke setPrivateKey method", e );
        }
    }

    private void setPassphrase(String passphrase, Object delegate) {
        try {
            getPassphraseSetter().invoke( delegate, passphrase );
        }
        catch (Exception e) {
            throw new ReflectionException( "Unable to invoke setPassphrase method", e );
        }
    }

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	private Method idGetterMethod;

	public Method idGetterMethod() {
		if ( idGetterMethod == null ) {
			idGetterMethod = locateIdGetterMethod();
		}
		return idGetterMethod;
	}

	private Method locateIdGetterMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "getId" );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate getId method", e );
		}
	}

	private Method urlGetterMethod;

	public Method urlGetterMethod() {
		if ( urlGetterMethod == null ) {
			urlGetterMethod = locateUrlGetterMethod();
		}
		return urlGetterMethod;
	}

	private Method locateUrlGetterMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "getUrl" );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate getUrl method", e );
		}
	}

	private Method addAuthenticationMethod;

	private Method authenticationAdderMethod() {
		if ( addAuthenticationMethod == null ) {
			addAuthenticationMethod = locateAuthenticationAdderMethod();
		}
		return addAuthenticationMethod;
	}

	private Method locateAuthenticationAdderMethod() {
		try {
			return getRemoteRepositoryClass().getMethod( "addAuthentication", getAuthClass() );
		}
		catch (NoSuchMethodException e) {
			throw new ReflectionException( "Could not locate addAuthentication method", e );
		}
	}

	private static final String REMOTE_REPO_CLASS_NAME = "org.apache.maven.artifact.ant.RemoteRepository";
	private Class remoteRepoClass;

	public Class getRemoteRepositoryClass() {
		if ( remoteRepoClass == null ) {
			remoteRepoClass = locateRemoteRepositoryClass();
		}
		return remoteRepoClass;
	}

	private Class locateRemoteRepositoryClass() {
		try {
			return classLoader.loadClass(REMOTE_REPO_CLASS_NAME);
		}
		catch (ClassNotFoundException e) {
			throw new ReflectionException( "Unable to locate class [" + REMOTE_REPO_CLASS_NAME + "]", e );
		}
	}

    private Method userNameSetterMethod;

    private Method getUserNameSetter() {
        if ( userNameSetterMethod == null ) {
            userNameSetterMethod = locateUserNameSetter();
        }
        return userNameSetterMethod;
    }

    private Method locateUserNameSetter() {
        try {
            return getAuthClass().getMethod( "setUserName", String.class );
        }
        catch (NoSuchMethodException e) {
            throw new ReflectionException( "Could not locate setUserName method", e );
        }
    }

    private Method passwordSetterMethod;

    private Method getPasswordSetter() {
        if ( passwordSetterMethod == null ) {
            passwordSetterMethod = locatePasswordSetter();
        }
        return passwordSetterMethod;
    }

    private Method locatePasswordSetter() {
        try {
            return getAuthClass().getMethod( "setPassword", String.class );
        }
        catch (NoSuchMethodException e) {
            throw new ReflectionException( "Could not locate setPassword method", e );
        }
    }

    private Method privateKeySetterMethod;

    private Method getPrivateKeySetter() {
        if ( privateKeySetterMethod == null ) {
            privateKeySetterMethod = locatePrivateKeySetter();
        }
        return privateKeySetterMethod;
    }

    private Method locatePrivateKeySetter() {
        try {
            return getAuthClass().getMethod( "setPrivateKey", String.class );
        }
        catch (NoSuchMethodException e) {
            throw new ReflectionException( "Could not locate setPrivateKey method", e );
        }
    }

    private Method passphraseSetterMethod;

    private Method getPassphraseSetter() {
        if ( passphraseSetterMethod == null ) {
            passphraseSetterMethod = locatePassphraseSetter();
        }
        return passphraseSetterMethod;
    }

    private Method locatePassphraseSetter() {
        try {
            return getAuthClass().getMethod( "setPassphrase", String.class );
        }
        catch (NoSuchMethodException e) {
            throw new ReflectionException( "Could not locate setPassphrase method", e );
        }
    }

    private static final String AUTH_CLASS_NAME = "org.apache.maven.artifact.ant.Authentication";
    private Class authClass;

    public Class getAuthClass() {
        if ( authClass == null ) {
            authClass = locateAuthClass();
        }
        return authClass;
    }

    private Class locateAuthClass() {
        try {
            return classLoader.loadClass( AUTH_CLASS_NAME );
        }
        catch (ClassNotFoundException e) {
            throw new ReflectionException( "Unable to locate class [" + AUTH_CLASS_NAME + "]", e );
        }
    }

}
