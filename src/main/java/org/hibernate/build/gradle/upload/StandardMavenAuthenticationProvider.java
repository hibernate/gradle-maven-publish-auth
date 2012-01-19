/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * Copyright (c) 2011, Red Hat Inc. or third-party contributors as
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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;
import org.sonatype.plexus.components.sec.dispatcher.DefaultSecDispatcher;
import org.sonatype.plexus.components.sec.dispatcher.SecDispatcherException;
import org.sonatype.plexus.components.sec.dispatcher.SecUtil;
import org.sonatype.plexus.components.sec.dispatcher.model.SettingsSecurity;
import org.xml.sax.InputSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provider of {@link org.apache.maven.artifact.ant.RemoteRepository} {@link org.apache.maven.artifact.ant.Authentication} based on standard Maven
 * conventions using {@literal settings.xml}.
 *
 * @author Steve Ebersole
 */
public class StandardMavenAuthenticationProvider implements AuthenticationProvider {
	private static final Logger log = LoggerFactory.getLogger( StandardMavenAuthenticationProvider.class );

	public static final String SETTINGS_LOCATION_OVERRIDE = "maven.settings";

	private ConcurrentHashMap<String,MavenAuthentication> repositoryAuthenticationMap;

	@Override
	public MavenAuthentication determineAuthentication(MavenRepository remoteRepository) {
		if ( repositoryAuthenticationMap == null ) {
			loadRepositoryAuthenticationMap();
		}

		return repositoryAuthenticationMap.get( remoteRepository.getId() );
	}

	private PasswordReader determinePasswordReader() {
		final File securitySettingsFile = determineSecuritySettingsFileLocation();
		final String masterPasswordEntry = securitySettingsFile.exists()
				? extractMasterPassword( securitySettingsFile )
				: null;

		System.out.println( "master password entry : " + masterPasswordEntry );

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

	private void loadRepositoryAuthenticationMap() {
		repositoryAuthenticationMap = new ConcurrentHashMap<String, MavenAuthentication>();

		final PasswordReader passwordReader = determinePasswordReader();
		final File settingsFile = determineSettingsFileLocation();
		try {
			InputSource inputSource = new InputSource( new FileInputStream( settingsFile ) );
			try {
				final Document document = buildSAXReader().read( inputSource );
				final Element settingsElement = document.getRootElement();
				final Element serversElement = settingsElement.element( "servers" );
				final Iterator serversIterator = serversElement.elementIterator( "server" );
				while ( serversIterator.hasNext() ) {
					final Element serverElement = (Element) serversIterator.next();
					final String id = extractValue( serverElement.element( "id" ) );
					if ( id == null ) {
						continue;
					}
					final MavenAuthentication authentication = extractServerValues( serverElement, passwordReader );
					repositoryAuthenticationMap.put( id, authentication );
				}
			}
			catch (DocumentException e) {
				log.error( "Error reading Maven settings.xml", e );
			}
		}
		catch ( FileNotFoundException e ) {
			log.info( "Unable to locate Maven settings.xml" );
		}
	}

	private MavenAuthentication extractServerValues(Element serverElement, PasswordReader passwordReader) {
		final MavenAuthentication authentication = new MavenAuthentication();
		authentication.setUserName( extractValue( serverElement.element( "username" ) ) );
		authentication.setPassword( passwordReader.readPassword( serverElement.element( "password" ) ) );
		authentication.setPrivateKey( extractValue( serverElement.element( "privateKey" ) ) );
		authentication.setPassphrase( extractValue( serverElement.element( "passphrase" ) ) );
		return authentication;
	}

	private static String extractValue(Element element) {
		if ( element == null ) {
			return null;
		}

		final String value = element.getTextTrim();
		if ( value != null && value.length() == 0 ) {
			return null;
		}

		return value;
	}

	private SAXReader buildSAXReader() {
		SAXReader saxReader = new SAXReader( new DocumentFactory() );
		saxReader.setMergeAdjacentText( true );
		return saxReader;
	}

	private File determineSecuritySettingsFileLocation() {
		final String defaultLocation = "~/.m2/settings-security.xml";
		final String location = System.getProperty( DefaultSecDispatcher.SYSTEM_PROPERTY_SEC_LOCATION, defaultLocation );
		return new File( normalizePath( location ) );
	}

	private static String normalizePath(String path) {
		if ( path.startsWith( "~" ) ) {
			path = System.getProperty( "user.home" ) + path.substring( 1 );
		}
		return path;
	}

	private File determineSettingsFileLocation() {
		final String defaultLocation = "~/.m2/settings.xml";
		final String location = System.getProperty( SETTINGS_LOCATION_OVERRIDE, defaultLocation );
		return new File( normalizePath( location ) );
	}

	private static interface PasswordReader {
		public String readPassword(Element passwordElement);
	}

	private static class BasicPasswordReader implements PasswordReader {
		@Override
		public String readPassword(Element passwordElement) {
			return extractValue( passwordElement );
		}
	}

	private static class PlexusCipherPasswordReader implements PasswordReader {
		private final DefaultPlexusCipher cipher;
		private final String master;

		private PlexusCipherPasswordReader(String master) {
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
			final String value = extractValue( passwordElement );
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
