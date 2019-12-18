/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import java.util.Locale;
import java.util.Objects;

import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.internal.project.ProjectInternal;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.tasks.PublishToMavenRepository;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.Upload;
import org.gradle.testfixtures.ProjectBuilder;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

/**
 * @author Steve Ebersole
 */
public class AuthTests {

	@Rule
	public final EnvironmentVariables environmentVariables = new EnvironmentVariables();

	@Test
	public void testBasicPluginFunction() {
		System.setProperty(
				SettingsXmlCredentialsProvider.SETTINGS_LOCATION_OVERRIDE,
				TestHelper.settingsXmlFile().getAbsolutePath()
		);

		final ProjectBuilder projectBuilder = ProjectBuilder.builder().withProjectDir( TestHelper.projectDirectory( "simple" ) );
		final Project project = projectBuilder.build();

		project.getPluginManager().apply( "maven" );
		project.getPluginManager().apply( "maven-publish" );


//		// Define a configuration for published artifacts - legacy publishing needs this
//		final Configuration publishedArtifactsConfiguration = project.getConfigurations().create( "publishedArtifacts" );
//		project.getArtifacts().add( publishedArtifactsConfiguration.getName(), project.file( "build/pubs" ) );

		final String serverId = "authenticated-server";

		applyRepositories( project, serverId );

		project.getPluginManager().apply( MavenRepoAuthPlugin.class );
		( (ProjectInternal) project ).evaluate();

		verifyCredentials( project, serverId, "tron", "user" );
	}

	@Test
	public void testPluginEncryptedFunction() {
		System.setProperty(
				SettingsXmlCredentialsProvider.SETTINGS_LOCATION_OVERRIDE,
				TestHelper.encryptedSettingsXmlFile().getAbsolutePath()
		);

		System.setProperty(
				"settings.security",
				TestHelper.securitySettingsXmlFile().getAbsolutePath()
		);

		final ProjectBuilder projectBuilder = ProjectBuilder.builder().withProjectDir( TestHelper.projectDirectory( "encrypted" ) );
		final Project project = projectBuilder.build();

		project.getPluginManager().apply( "maven" );
		project.getPluginManager().apply( "maven-publish" );

		final String serverId = "authenticated-encrypted-server";

		applyRepositories( project, serverId );

		project.getPluginManager().apply( MavenRepoAuthPlugin.class );
		( (ProjectInternal) project ).evaluate();

		verifyCredentials( project, serverId, "clu", "xyz" );
	}

	@Test
	public void testPluginEnvironmentFunction() {
		environmentVariables.set( "SERVER_USERNAME", "clu" );
		environmentVariables.set( "SERVER_PASSWORD", "xyz" );

		System.setProperty(
				SettingsXmlCredentialsProvider.SETTINGS_LOCATION_OVERRIDE,
				TestHelper.environmentSettingsXmlFile().getAbsolutePath()
		);

		final ProjectBuilder projectBuilder = ProjectBuilder.builder().withProjectDir( TestHelper.projectDirectory( "simple" ) );
		final Project project = projectBuilder.build();

		project.getPluginManager().apply( "maven" );
		project.getPluginManager().apply( "maven-publish" );

		final String serverId = "environment-server";

		applyRepositories( project, serverId );

		project.getPluginManager().apply( MavenRepoAuthPlugin.class );
		( (ProjectInternal) project ).evaluate();

		verifyCredentials( project, serverId, "clu", "xyz" );
	}


	private void applyRepositories(Project project, String repoName) {
		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Define the dependency repo

		project.getRepositories().maven(
				repo -> {
					repo.setName( repoName );
					repo.setUrl( "http://www.nowhere.com" );
				}
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// define a publishing repo

		final PublishingExtension publishingExtension = project.getExtensions().findByType( PublishingExtension.class );
		assert publishingExtension != null;
		publishingExtension.getRepositories().maven(
				repo -> {
					repo.setName( repoName );
					repo.setUrl( "http://www.nowhere.com" );
				}
		);
		publishingExtension.getPublications().create(
				"main",
				MavenPublication.class
		);


		// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// Apply the legacy maven upload support

		final Upload uploadArchivesTask = (Upload) project.getTasks().findByName( "uploadArchives" );
		uploadArchivesTask.getRepositories().maven(
				repo -> {
					repo.setName( repoName );
					repo.setUrl( "http://www.nowhere.com" );
				}
		);
	}

	private void verifyCredentials(
			Project project,
			String serverId,
			String expectedUsername,
			String expectedPassword) {
		// artifact repo
		final MavenArtifactRepository repo = (MavenArtifactRepository) project.getRepositories().getByName( serverId );
		check( expectedUsername, expectedPassword, repo );

		// legacy upload task
		final Upload uploadArchivesTask = (Upload) project.getTasks().findByName( "uploadArchives" );
		assert uploadArchivesTask != null;
		final MavenArtifactRepository uploadRepo = (MavenArtifactRepository) uploadArchivesTask.getRepositories().getByName( serverId );
		check( expectedUsername, expectedPassword, uploadRepo );

		// publishing
		final TaskCollection<PublishToMavenRepository> publishTasks = project.getTasks().withType( PublishToMavenRepository.class );
		for ( PublishToMavenRepository publishTask : publishTasks ) {
			if ( ! publishTask.getRepository().getName().equals( serverId ) ) {
				continue;
			}

			check( expectedUsername, expectedPassword, publishTask.getRepository() );
		}

	}

	private void check(String expectedUsername, String expectedPassword, MavenArtifactRepository repo) {
		check( expectedUsername, repo.getCredentials().getUsername() );
		check( expectedPassword, repo.getCredentials().getPassword() );
	}

	private void check(Object expected, Object actual) {
		if ( !Objects.equals( expected, actual ) ) {
			throw new RuntimeException(
					String.format(
							Locale.ROOT,
							"Expected `%s`, but found `%s`",
							expected,
							actual
					)
			);
		}
	}
}
