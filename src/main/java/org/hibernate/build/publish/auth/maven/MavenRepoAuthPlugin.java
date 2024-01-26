/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import org.hibernate.build.publish.auth.CredentialsProviderRegistry;

/**
 * @author Steve Ebersole
 */
@SuppressWarnings("unused")
public class MavenRepoAuthPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		final MavenRepoAuthExtension extension = new MavenRepoAuthExtension(
				new CredentialsProviderRegistry( new SettingsXmlCredentialsProvider() )
		);
		project.getExtensions().add( MavenRepoAuthExtension.NAME, extension );
		doApply( project, extension.credentialsProviderRegistry() );
	}

	public static void doApply(Project project, CredentialsProviderRegistry registry) {
		PublishingRepoHandler.apply( project, registry );
		DependencyRepoHandler.apply( project, registry );
	}
}
