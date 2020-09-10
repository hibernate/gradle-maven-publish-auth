/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven.pwd;

import java.io.File;

import org.hibernate.build.publish.util.PathHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import static org.hibernate.build.publish.auth.maven.pwd.DecryptionPasswordStrategy.decrypt;

/**
 * @author Steve Ebersole
 */
public class PasswordProcessor {
	private static final String DEFAULT_SECURITY_SETTINGS_LOCATION = "~/.m2/settings-security.xml";

	private static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	/**
	 * Singleton access
	 */
	public static final PasswordProcessor INSTANCE = new PasswordProcessor();

	private final PlexusCipher cipher = buildCipher();
	private DecryptionPasswordStrategy decryptionStrategy;

	public PasswordStrategy resolvePasswordStrategy(String password) {
		if ( cipher != null && cipher.isEncryptedString( password ) ) {
			if ( decryptionStrategy == null ) {
				final File securitySettingsFile = determineSecuritySettingsFileLocation();
				final String encryptedMasterPassword = securitySettingsFile.exists()
						? extractMasterPassword( securitySettingsFile )
						: null;

				if ( encryptedMasterPassword != null ) {
					log.debug( "Encrypted master password: " + encryptedMasterPassword );

					final String passPhrase = decrypt(
							cipher,
							encryptedMasterPassword,
							DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION
					);

					if ( passPhrase != null ) {
						decryptionStrategy = new DecryptionPasswordStrategy( cipher, passPhrase );
					}
				}
			}

			return decryptionStrategy;
		}

		return DefaultPasswordStrategy.INSTANCE;
	}

	private String extractMasterPassword(File securitySettingsFile) {
		try {
			SettingsSecurity settingsSecurity = SecUtil.read( securitySettingsFile.getAbsolutePath(), true );
			return settingsSecurity == null ? null : settingsSecurity.getMaster();
		}
		catch ( SecDispatcherException e) {
			log.warn( "Unable to read Maven security settings file", e );
			return null;
		}
	}

	private File determineSecuritySettingsFileLocation() {
		String location = System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, DEFAULT_SECURITY_SETTINGS_LOCATION );
		return new File( PathHelper.normalizePath( location ) );
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
