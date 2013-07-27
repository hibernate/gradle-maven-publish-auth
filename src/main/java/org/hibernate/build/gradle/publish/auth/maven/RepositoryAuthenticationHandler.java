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

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.artifacts.repositories.PasswordCredentials;

/**
 * 
 * @author Artur Kotyrba
 */
public class RepositoryAuthenticationHandler implements Action<MavenArtifactRepository> {
	private final CredentialsProviderRegistry credentialsProviderRegistry;

	public RepositoryAuthenticationHandler(CredentialsProviderRegistry credentialsProviderRegistry) {
		this.credentialsProviderRegistry = credentialsProviderRegistry;
	}

	public void applyTo(Project project) {
		project.getRepositories().withType( MavenArtifactRepository.class ).all( this );
	}

	@Override
	public void execute(MavenArtifactRepository mavenArtifactRepository) {
		final String id = mavenArtifactRepository.getName();
		final Credentials credentials = locateAuthenticationCredentials( id );
		if ( credentials == null ) {
			return;
		}

		PasswordCredentials passwordCredentials = mavenArtifactRepository.getCredentials();
		passwordCredentials.setUsername( credentials.getUserName() );
		passwordCredentials.setPassword( credentials.getPassword() );
	}

	private Credentials locateAuthenticationCredentials(String repositoryId) {
		for ( CredentialsProvider provider : credentialsProviderRegistry.providers() ) {
			Credentials authentication = provider.determineAuthentication( repositoryId );
			if ( authentication != null ) {
				return authentication;
			}
		}
		return null;
	}
}
