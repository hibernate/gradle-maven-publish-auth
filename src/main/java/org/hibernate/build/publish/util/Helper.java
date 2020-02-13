/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.util;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;
import org.hibernate.build.publish.auth.Credentials;
import org.hibernate.build.publish.auth.CredentialsProvider;
import org.hibernate.build.publish.auth.CredentialsProviderRegistry;

/**
 * @author Steve Ebersole
 */
public class Helper {
	private static final Set<String> AUTHENTICATABLE_PROTOCOLS =
			Stream.of( "http", "https", "sftp" )
					.collect( Collectors.toCollection( HashSet::new ) );

	public static void applyCredentials(
			MavenArtifactRepository repo,
			CredentialsProviderRegistry credentialsProviderRegistry) {
		if ( !AUTHENTICATABLE_PROTOCOLS.contains( repo.getUrl().getScheme().toLowerCase() ) ) {
				return;
		}

		final Credentials credentials = locateAuthenticationCredentials( repo.getName(), credentialsProviderRegistry );

		if ( credentials == null ) {
			return;
		}

		final PasswordCredentials repoCredentials = repo.getCredentials();
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
