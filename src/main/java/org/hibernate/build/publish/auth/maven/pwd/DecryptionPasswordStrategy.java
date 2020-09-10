/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * A {@link PasswordProcessor} which decrypts encrypted Maven passwords using the master password
 * from {@code ~/.m2/settings-security.xml}.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class DecryptionPasswordStrategy implements PasswordStrategy {

	private static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	private final PlexusCipher cipher;
	private final String masterPassword;

	public DecryptionPasswordStrategy(PlexusCipher cipher, String masterPassword) {
		this.cipher = cipher;
		this.masterPassword = masterPassword;
	}

	@Override
	public String interpretPassword(String password) {
		return decrypt( cipher, password, masterPassword );
	}

	public static String decrypt(PlexusCipher cipher, String encryptedPassword, String passPhrase) {
		try {
			return cipher.decryptDecorated( encryptedPassword, passPhrase );
		}
		catch (PlexusCipherException e) {
			log.warn( "Unable to decrypt Maven password using PlexusCipher", e );
			return encryptedPassword;
		}
	}
}
