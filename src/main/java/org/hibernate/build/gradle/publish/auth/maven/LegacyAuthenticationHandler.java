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
package org.hibernate.build.gradle.publish.auth.maven;

import java.lang.reflect.Method;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.maven.MavenDeployer;
import org.gradle.api.tasks.Upload;

/**
 * Acts as the main authentication coordinator for the upload.  It will delegate to all {@link CredentialsProvider}
 * instances registered with the {@link CredentialsProviderRegistry} looking for any that provide authentication
 * against the maven repository defined for each upload task.
 * <p/>
 * <strong>
 *     IMPL NOTE : This will need to change drastically whenever Gradle moves to its {@code Publication} scheme for uploads.
 * </strong>
 *
 * @author Steve Ebersole
 */
public class LegacyAuthenticationHandler implements Action<Upload> {
	private final CredentialsProviderRegistry credentialsProviderRegistry;

	public LegacyAuthenticationHandler(CredentialsProviderRegistry credentialsProviderRegistry) {
		this.credentialsProviderRegistry = credentialsProviderRegistry;
	}

	public void applyTo(Project project) {
		final Action authAction = this;

		project.getTasks().withType( Upload.class ).all(
				new Action<Upload>() {
					@Override
					@SuppressWarnings( {"unchecked"} )
					public void execute(final Upload uploadTask) {
						if ( ! uploadTask.getRepositories().withType( MavenDeployer.class ).isEmpty() ) {
							uploadTask.doFirst( authAction );
						}
					}
				}
		);
	}

	@Override
	public void execute(Upload upload) {
		upload.getRepositories().withType( MavenDeployer.class ).all(
				new Action<MavenDeployer>() {
					@Override
					public void execute(MavenDeployer deployer) {
						final Object repositoryDelegate = deployer.getRepository();
						if ( repositoryDelegate != null ) {
							final Repository repository = new Repository( repositoryDelegate, deployer.getClass().getClassLoader() );
							final Credentials authentication = locateAuthenticationDetails( repository );
							if ( authentication != null ) {
								repository.addAuthentication( authentication );
							}
						}
						final Object snapshotRepositoryDelegate = deployer.getSnapshotRepository();
						if ( snapshotRepositoryDelegate != null ) {
							final Repository snapshotRepository = new Repository( snapshotRepositoryDelegate, deployer.getClass().getClassLoader() );
							final Credentials authentication = locateAuthenticationDetails( snapshotRepository );
							if ( authentication != null ) {
								snapshotRepository.addAuthentication( authentication );
							}
						}
					}
				}
		);
	}

	private Credentials locateAuthenticationDetails(Repository repository) {
		final String repositoryId = repository.getId();
		for ( CredentialsProvider provider : credentialsProviderRegistry.providers() ) {
			Credentials authentication = provider.determineAuthentication( repositoryId );
			if ( authentication != null ) {
				return authentication;
			}
		}
		return null;
	}

	/**
	 * A wrapper around the {@link org.apache.maven.artifact.ant.RemoteRepository} class from the maven ant tasks due to
	 * some change in Gradle causing classloader problems.
	 */
	public static class Repository {
		private final Object delegate;
		private final ClassLoader classLoader;

		public Repository(Object delegate, ClassLoader classLoader) {
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

		public void addAuthentication(Credentials authentication) {
			try {
				authenticationAdderMethod().invoke( delegate, getDelegate(authentication) );
			}
			catch (Exception e) {
				throw new ReflectionException( "Unable to invoke addAuthentication method", e );
			}
		}


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		private Object getDelegate(Credentials authentication) {
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

		@SuppressWarnings( {"unchecked"} )
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

		@SuppressWarnings( {"unchecked"} )
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

		@SuppressWarnings( {"unchecked"} )
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

		public Class<?> getAuthClass() {
			if ( authClass == null ) {
				authClass = locateAuthClass();
			}
			return authClass;
		}

		private Class<?> locateAuthClass() {
			try {
				return classLoader.loadClass( AUTH_CLASS_NAME );
			}
			catch (ClassNotFoundException e) {
				throw new ReflectionException( "Unable to locate class [" + AUTH_CLASS_NAME + "]", e );
			}
		}

	}

	public static class ReflectionException extends RuntimeException {
		public ReflectionException(String message) {
			super( message );
		}

		public ReflectionException(String message, Throwable cause) {
			super( message, cause );
		}
	}
}
