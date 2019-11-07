/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.publish.PublishingExtension;

import org.hibernate.build.publish.auth.CredentialsProviderRegistry;
import org.hibernate.build.publish.util.Helper;

/**
 * Acts as the main authentication coordinator for "publish" tasks using the Publication API.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")
public class PublishingRepoHandler {
	public static void apply(Project project, CredentialsProviderRegistry credentialsProviderRegistry) {
		project.afterEvaluate(
				p -> p.getExtensions().getByType( PublishingExtension.class ).getRepositories().forEach(
						repo -> {
							if ( repo instanceof MavenArtifactRepository ) {
								final MavenArtifactRepository mavenRepo = (MavenArtifactRepository) repo;

								Helper.applyCredentials(
										mavenRepo.getName(),
										mavenRepo.getCredentials(),
										credentialsProviderRegistry
								);
							}
						}
				)
		);
	}
}
