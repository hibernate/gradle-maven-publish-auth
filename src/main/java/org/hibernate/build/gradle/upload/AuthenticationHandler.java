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
package org.hibernate.build.gradle.upload;

import org.gradle.api.Action;
import org.gradle.api.artifacts.maven.MavenDeployer;
import org.gradle.api.tasks.Upload;

/**
 * Acts as the main authentication coordinator for the upload.  It will delegate to all {@link AuthenticationProvider}
 * instances registered with the {@link AuthenticationProviderRegistry} looking for any that provide authentication
 * against the maven repository defined for each upload task.
 * <p/>
 * <strong>
 *     IMPL NOTE : This will need to change drastically whenever Gradle moves to its {@code Publication} scheme for uploads.
 * </strong>
 *
 * @author Steve Ebersole
 */
public class AuthenticationHandler implements Action<Upload> {
	private final AuthenticationProviderRegistry authenticationProviderRegistry;

	public AuthenticationHandler(AuthenticationProviderRegistry authenticationProviderRegistry) {
		this.authenticationProviderRegistry = authenticationProviderRegistry;
	}

	@Override
	public void execute(Upload upload) {
		// todo : unfortunately I have no idea how to apply this to non MavenDeployer-type repos...
		upload.getRepositories().withType( MavenDeployer.class ).all(
				new Action<MavenDeployer>() {
					public void execute(MavenDeployer deployer) {
						final Object repositoryDelegate = deployer.getRepository();
						if ( repositoryDelegate != null ) {
							final MavenRepository repository = new MavenRepository( repositoryDelegate, deployer.getClass().getClassLoader() );
							final MavenAuthentication authentication = locateAuthenticationDetails( repository );
							if ( authentication != null ) {
								repository.addAuthentication( authentication );
							}
						}
						final Object snapshotRepositoryDelegate = deployer.getSnapshotRepository();
						if ( snapshotRepositoryDelegate != null ) {
							final MavenRepository snapshotRepository = new MavenRepository( snapshotRepositoryDelegate, deployer.getClass().getClassLoader() );
							final MavenAuthentication authentication = locateAuthenticationDetails( snapshotRepository );
							if ( authentication != null ) {
								snapshotRepository.addAuthentication( authentication );
							}
						}
					}
				}
		);
	}

	private MavenAuthentication locateAuthenticationDetails(MavenRepository repository) {
		for ( AuthenticationProvider provider : authenticationProviderRegistry.providers() ) {
			MavenAuthentication authentication = provider.determineAuthentication( repository );
			if ( authentication != null ) {
				return authentication;
			}
		}
		return null;
	}
}
