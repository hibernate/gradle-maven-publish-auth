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

import org.gradle.api.Plugin;
import org.gradle.api.Project;

/**
 * Manages authentication aspects of artifact publishing.
 *
 * @author Steve Ebersole
 */
public class AuthenticationManager implements Plugin<Project> {
	@Override
	public void apply(final Project project) {
		final CredentialsProviderRegistry registry = new CredentialsProviderRegistry( project );

		final PublishingAuthenticationHandler publishingHandler = new PublishingAuthenticationHandler( registry );
		publishingHandler.applyTo( project );

		final LegacyAuthenticationHandler legacyHandler = new LegacyAuthenticationHandler( registry );
		legacyHandler.applyTo( project );

		final RepositoryAuthenticationHandler repositoryHandler = new RepositoryAuthenticationHandler( registry );
		repositoryHandler.applyTo( project );
	}
}
