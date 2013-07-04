/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * JBoss, Home of Professional Open Source
 * Copyright 2013 Red Hat Inc. and/or its affiliates and other contributors
 * as indicated by the @authors tag. All rights reserved.
 * See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This copyrighted material is made available to anyone wishing to use,
 * modify, copy, or redistribute it subject to the terms and conditions
 * of the GNU Lesser General Public License, v. 2.1.
 * This program is distributed in the hope that it will be useful, but WITHOUT A
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A
 * PARTICULAR PURPOSE.  See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License,
 * v.2.1 along with this distribution; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 */
package org.hibernate.build.gradle.publish.auth.maven.passwordprocessor;

import java.io.File;

import org.hibernate.build.gradle.publish.auth.maven.PathHelper;
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
class DecryptionProcessor extends PasswordProcessor {

	private static final String DEFAULT_SECURITY_SETTINGS_LOCATION = "~/.m2/settings-security.xml";

	private static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	private final String master;

	public DecryptionProcessor(String password, PlexusCipher cipher) {
		super( password, cipher );
		this.master = getMasterPassword();
	}

	@Override
	public String processPassword() {
		return decrypt( password, master );
	}

	private String getMasterPassword() {
		File securitySettingsFile = determineSecuritySettingsFileLocation();
		String encryptedMasterPassword = securitySettingsFile.exists() ?
				extractMasterPassword( securitySettingsFile ) :
					null;

		log.trace( "Encrypted master password: " + encryptedMasterPassword );

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
