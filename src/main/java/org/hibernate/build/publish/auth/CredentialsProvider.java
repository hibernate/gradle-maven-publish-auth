/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth;

/**
 * Contract for providers of authentication credentials for authenticating against remote repositories.
 *
 * @author Steve Ebersole
 */
public interface CredentialsProvider {
	/**
	 * Given a repository identifier and url, determine the authentication credentials
	 * according to this provider's contract.  Return {@code null} to indicate no
	 * authentication applied for this repository by this provider.
	 *
	 * @param repoId The id of the repository to check for authentication details.
	 *
	 * @return The authentication details, or {@code null} to indicate none.
	 */
	Credentials determineAuthentication(String repoId);
}
