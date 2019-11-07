/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * @author Steve Ebersole
 */
public class PasswordProcessor {
	public static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	/**
	 * Singleton access
	 */
	public static final PasswordProcessor INSTANCE = new PasswordProcessor();

	private final PlexusCipher cipher = buildCipher();
	private DecryptionPasswordStrategy decryptionStrategy;

	public PasswordStrategy resolvePasswordStrategy(String password) {
		if ( cipher == null || !cipher.isEncryptedString( password ) ) {
			return DefaultPasswordStrategy.INSTANCE;
		}
		else {
			if ( decryptionStrategy == null ) {
				decryptionStrategy = new DecryptionPasswordStrategy( cipher );
			}
			return decryptionStrategy;
		}
	}

	private static PlexusCipher buildCipher() {
		try {
			return new DefaultPlexusCipher();
		}
		catch (PlexusCipherException e) {
			log.error( "Unable to create PlexusCipher in order to decrypt Maven passwords" );
			return null;
		}
	}
}
