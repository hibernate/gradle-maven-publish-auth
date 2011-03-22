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
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.tasks.Upload;

/**
 * Manages authentication aspects of artifact uploading by delegation to registered {@link AuthenticationProvider}
 * instances.
 *
 * @author Steve Ebersole
 */
public class UploadAuthenticationManager implements Plugin<Project> {

	@Override
	public void apply(final Project project) {
		// todo : ideally the registry would be handled by a convention to allow configuration (aka, adding more providers)...
		//		for our purposes here in Hibernate we only care about the Maven settings.xml based way so we
		//		code for just that.
		final AuthenticationProviderRegistry registry = new AuthenticationProviderRegistry();
		final Action authenticationHandler = new AuthenticationHandler( registry );

		project.getTasks().withType( Upload.class ).all(
			new Action<Upload>() {
				@Override
				@SuppressWarnings( {"unchecked"})
				public void execute(final Upload uploadTask) {
					uploadTask.doFirst( authenticationHandler );
				}
			}
		);
	}

}
