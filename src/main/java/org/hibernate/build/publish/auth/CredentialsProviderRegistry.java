/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth;

import java.util.LinkedList;

/**
 *
 * A registry of {@link CredentialsProvider} instances.
 *
 * @author Steve Ebersole
 */
@SuppressWarnings("unused")
public class CredentialsProviderRegistry {
	private final LinkedList<CredentialsProvider> credentialsProviders = new LinkedList<>();

	public CredentialsProviderRegistry() {
	}

	public CredentialsProviderRegistry(CredentialsProvider credentialsProvider) {
		credentialsProviders.add( credentialsProvider );
	}

	public void appendAuthenticationProvider(CredentialsProvider provider) {
		credentialsProviders.addLast( provider );
	}

	public void prependAuthenticationProvider(CredentialsProvider provider) {
		credentialsProviders.addFirst( provider );
	}

	public Iterable<CredentialsProvider> providers() {
		return credentialsProviders;
	}
}
