/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import java.util.regex.Pattern;

/**
 * A {@link PasswordProcessor} for unencrypted Maven passwords which unescapes the given clear text password in case it
 * contains escaped curly braces.
 *
 * @author Gunnar Morling
 * @author Steve Ebersole
 */
public class DefaultPasswordStrategy implements PasswordStrategy {
	/**
	 * Singleton access
	 */
	public static final DefaultPasswordStrategy INSTANCE = new DefaultPasswordStrategy();

	@Override
	public String interpretPassword(String password) {
		return password.replaceAll( Pattern.quote( "\\{" ), "{" )
				.replaceAll( Pattern.quote( "\\}" ), "}" );
	}
}
