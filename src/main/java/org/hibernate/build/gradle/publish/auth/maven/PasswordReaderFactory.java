/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2013, Red Hat Inc. or third-party contributors as
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

import java.io.File;

import org.dom4j.Element;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Ebersole
 */
public class PasswordReaderFactory {
	private static final Logger log = LoggerFactory.getLogger( PasswordReaderFactory.class );

	public static final PasswordReaderFactory INSTANCE = new PasswordReaderFactory();

	public PasswordReader determinePasswordReader() {
		final File securitySettingsFile = determineSecuritySettingsFileLocation();
		final String masterPasswordEntry = securitySettingsFile.exists()
				? extractMasterPassword( securitySettingsFile )
				: null;

		log.trace( "master password entry : " + masterPasswordEntry );

		return masterPasswordEntry == null
				? new BasicPasswordReader()
				: new PlexusCipherPasswordReader( masterPasswordEntry );
	}

	private String extractMasterPassword(File securitySettingsFile) {
		try {
			SettingsSecurity settingsSecurity = SecUtil.read( securitySettingsFile.getAbsolutePath(), true );
			return settingsSecurity == null
					? null
					: settingsSecurity.getMaster();
		}
		catch (SecDispatcherException e) {
			log.warn( "Unable to read Maven security settings file", e );
			return null;
		}
	}

	private File determineSecuritySettingsFileLocation() {
		final String defaultLocation = "~/.m2/settings-security.xml";
		final String location = System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, defaultLocation );
		return new File( PathHelper.normalizePath( location ) );
	}


	public static class BasicPasswordReader implements PasswordReader {
		@Override
		public String readPassword(Element passwordElement) {
			return DomHelper.extractValue( passwordElement );
		}
	}


	public static class PlexusCipherPasswordReader implements PasswordReader {
		private static final Logger log = LoggerFactory.getLogger( PlexusCipherPasswordReader.class );

		private final DefaultPlexusCipher cipher;
		private final String master;

		public PlexusCipherPasswordReader(String master) {
			this.cipher = buildCipher();
			this.master = decryptMaster( master, cipher );
		}

		private static DefaultPlexusCipher buildCipher() {
			try {
				return new DefaultPlexusCipher();
			}
			catch (PlexusCipherException e) {
				log.error( "Unable to create PlexusCipher in order to decrypt Maven passwords" );
				return null;
			}
		}

		private static String decryptMaster(String master, DefaultPlexusCipher cipher) {
			try {
				return cipher.decryptDecorated( master, DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION );
			}
			catch (PlexusCipherException e) {
				log.error( "Unable to create PlexusCipher in order to decrypt Maven passwords" );
				return null;
			}
		}

		@Override
		public String readPassword(Element passwordElement) {
			final String value = DomHelper.extractValue( passwordElement );
			try {
				return cipher.decryptDecorated( value, master );
			}
			catch (PlexusCipherException e) {
				log.warn( "Unable to decrypt Maven password using PlexusCipher", e );
				return value;
			}
		}
	}
}
