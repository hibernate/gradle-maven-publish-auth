/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import org.gradle.api.Project;
import org.gradle.api.artifacts.maven.MavenDeployer;
import org.gradle.api.artifacts.maven.MavenResolver;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.tasks.Upload;

import org.hibernate.build.publish.auth.CredentialsProvider;
import org.hibernate.build.publish.auth.CredentialsProviderRegistry;
import org.hibernate.build.publish.util.Helper;

/**
 * Acts as the main authentication coordinator for the upload.  It will delegate to all {@link CredentialsProvider}
 * instances registered with the {@link CredentialsProviderRegistry} looking for any that provide authentication
 * against the maven repository defined for each upload task.
 *
 * @author Steve Ebersole
 */
public class LegacyHandler {
	public static void apply(Project project, CredentialsProviderRegistry credentialsProviderRegistry) {
		project.afterEvaluate(
				p -> p.getTasks().withType( Upload.class )
						.forEach( upload -> process( upload, credentialsProviderRegistry ) )
		);
	}

	private static void process(Upload upload, CredentialsProviderRegistry credentialsProviderRegistry) {
		for ( ArtifactRepository repository : upload.getRepositories() ) {
			if ( repository instanceof MavenArtifactRepository ) {
				final MavenArtifactRepository mavenRepo = (MavenArtifactRepository) repository;
				Helper.applyCredentials(
						mavenRepo.getName(),
						mavenRepo.getCredentials(),
						credentialsProviderRegistry
				);
			}
		}
	}
}
