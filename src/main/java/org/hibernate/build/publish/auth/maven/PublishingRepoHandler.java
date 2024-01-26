/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

import org.hibernate.build.publish.auth.CredentialsProviderRegistry;
import org.hibernate.build.publish.util.Helper;

import java.lang.reflect.InvocationTargetException;

/**
 * Acts as the main authentication coordinator for "publish" tasks using the Publication API.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("WeakerAccess")
public class PublishingRepoHandler {
	public static void apply(Project project, CredentialsProviderRegistry credentialsProviderRegistry) {
		if( project.getExtensions().findByName( "publishing" ) == null ) {
			return;
		}

		project.afterEvaluate(
				p -> {
                    RepositoryHandler repos;
                    try {
						// load class by name, so we do not have to depend on the publishing plugin
						// org.gradle.api.publish.PublishingExtension.class
						var clazz = project.getClass().getClassLoader().loadClass( "org.gradle.api.publish.PublishingExtension" );
						Object publishingExtension = p.getExtensions().getByType(clazz);
                        repos = (RepositoryHandler) clazz.getMethod("getRepositories").invoke(publishingExtension);
                    } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException |
                             ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                    repos.forEach(
							repo -> {
								if (repo instanceof MavenArtifactRepository mavenRepo) {

									Helper.applyCredentials(
											mavenRepo,
											credentialsProviderRegistry
									);
								}
							}
					);
				}
		);
	}
}
