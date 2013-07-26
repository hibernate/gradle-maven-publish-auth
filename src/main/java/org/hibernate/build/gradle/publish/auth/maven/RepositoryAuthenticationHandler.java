package org.hibernate.build.gradle.publish.auth.maven;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;

public class RepositoryAuthenticationHandler implements Action<MavenArtifactRepository> {
	private final CredentialsProviderRegistry credentialsProviderRegistry;

	public RepositoryAuthenticationHandler(CredentialsProviderRegistry credentialsProviderRegistry) {
		this.credentialsProviderRegistry = credentialsProviderRegistry;
	}

	public void applyTo(Project project) {
		project.getRepositories().withType(MavenArtifactRepository.class).all(this);
	}

	@Override
	public void execute(MavenArtifactRepository mavenArtifactRepository) {
		final String id = mavenArtifactRepository.getName();
		final Credentials credentials = locateAuthenticationCredentials(id);

		if (credentials == null) {
			return;
		}

		mavenArtifactRepository.getCredentials().setUsername(credentials.getUserName());
		mavenArtifactRepository.getCredentials().setPassword(credentials.getPassword());
	}

	private Credentials locateAuthenticationCredentials(String repositoryId) {
		for (CredentialsProvider provider : credentialsProviderRegistry.providers()) {
			Credentials authentication = provider.determineAuthentication(repositoryId);

			if (authentication != null) {
				return authentication;
			}
		}

		return null;
	}
}
