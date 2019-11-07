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

import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

/**
 * A {@link PasswordProcessor} which decrypts encrypted Maven passwords using the master password
 * from {@code ~/.m2/settings-security.xml}.
 *
 * @author Steve Ebersole
 * @author Gunnar Morling
 */
public class DecryptionPasswordStrategy implements PasswordStrategy {

	private static final String DEFAULT_SECURITY_SETTINGS_LOCATION = "~/.m2/settings-security.xml";

	private static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	private final PlexusCipher cipher;
	private final String master;

	public DecryptionPasswordStrategy(PlexusCipher cipher) {
		this.cipher = cipher;
		this.master = getMasterPassword();
	}

	@Override
	public String interpretPassword(String password) {
		return decrypt( password, master );
	}

	private String getMasterPassword() {
		File securitySettingsFile = determineSecuritySettingsFileLocation();
		String encryptedMasterPassword = securitySettingsFile.exists()
				? extractMasterPassword( securitySettingsFile )
				: null;

		log.debug( "Encrypted master password: " + encryptedMasterPassword );

		return decrypt( encryptedMasterPassword, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );
	}

	private String extractMasterPassword(File securitySettingsFile) {
		try {
			SettingsSecurity settingsSecurity = SecUtil.read( securitySettingsFile.getAbsolutePath(), true );
			return settingsSecurity == null ? null : settingsSecurity.getMaster();
		}
		catch (SecDispatcherException e) {
			log.warn( "Unable to read Maven security settings file", e );
			return null;
		}
	}

	private File determineSecuritySettingsFileLocation() {
		String location = System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, DEFAULT_SECURITY_SETTINGS_LOCATION );
		return new File( PathHelper.normalizePath( location ) );
	}

	private String decrypt(String string, String password) {
		try {
			return cipher.decryptDecorated( string, password );
		}
		catch (PlexusCipherException e) {
			log.warn( "Unable to decrypt Maven password using PlexusCipher", e );
			return string;
		}
	}
}
