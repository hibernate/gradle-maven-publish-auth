/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.util;

import org.gradle.api.artifacts.repositories.PasswordCredentials;

import org.hibernate.build.publish.auth.Credentials;
import org.hibernate.build.publish.auth.CredentialsProvider;
import org.hibernate.build.publish.auth.CredentialsProviderRegistry;

/**
 * @author Steve Ebersole
 */
public class Helper {
	public static void applyCredentials(
			String repoId,
			PasswordCredentials repoCredentials,
			CredentialsProviderRegistry credentialsProviderRegistry) {
		final Credentials credentials = locateAuthenticationCredentials( repoId, credentialsProviderRegistry );

		if ( credentials == null ) {
			return;
		}

		repoCredentials.setUsername( credentials.getUserName() );
		repoCredentials.setPassword( credentials.getPassword() );

	}

	public static Credentials locateAuthenticationCredentials(
			String repositoryId,
			CredentialsProviderRegistry credentialsProviderRegistry) {
		for ( CredentialsProvider provider : credentialsProviderRegistry.providers() ) {
			Credentials authentication = provider.determineAuthentication( repositoryId );
			if ( authentication != null ) {
				return authentication;
			}
		}

		return null;
	}
}
