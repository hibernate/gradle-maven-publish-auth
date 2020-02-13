/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import org.hibernate.build.publish.auth.CredentialsProviderRegistry;
import org.hibernate.build.publish.util.Helper;

/**
 * Applies credentials to
 * @author Artur Kotyrba
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")
public class DependencyRepoHandler {
	public static void apply(Project project, CredentialsProviderRegistry credentialsProviderRegistry) {
		project.afterEvaluate(
				p -> p.getRepositories().withType( MavenArtifactRepository.class ).all(
						repo -> Helper.applyCredentials(
								repo,
								credentialsProviderRegistry
						)
				)
		);
	}
}
