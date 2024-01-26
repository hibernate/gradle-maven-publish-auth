/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.build.publish.auth.Credentials;
import org.hibernate.build.publish.auth.CredentialsProvider;
import org.hibernate.build.publish.auth.maven.pwd.PasswordProcessor;
import org.hibernate.build.publish.auth.maven.pwd.ValueProcessor;
import org.hibernate.build.publish.auth.maven.pwd.ValueProcessorRegex;
import org.hibernate.build.publish.util.DomHelper;
import org.hibernate.build.publish.util.PathHelper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;

/**
 * Provider of credentials based on standard Maven conventions using {@literal settings.xml}.
 *
 * @author Steve Ebersole
 */
public class SettingsXmlCredentialsProvider implements CredentialsProvider {
	private static final Logger log = LoggerFactory.getLogger( SettingsXmlCredentialsProvider.class );

	public static final String SETTINGS_LOCATION_OVERRIDE = "maven.settings";
	private static final String SETTINGS_DEFAULT_LOCATION = "~/.m2/settings.xml";

	private final Map<String,Credentials> credentialsByRepoIdMap;

	public final ValueProcessor valueProcessor = ValueProcessorRegex.INSTANCE;

	public SettingsXmlCredentialsProvider() {
		final File settingsFile = determineSettingsFileLocation();
		this.credentialsByRepoIdMap = extractCredentialsFromSettings( settingsFile );
	}

	private File determineSettingsFileLocation() {
		final String location = System.getProperty( SETTINGS_LOCATION_OVERRIDE, SETTINGS_DEFAULT_LOCATION );
		return new File( PathHelper.normalizePath( location ) );
	}

	private Map<String,Credentials> extractCredentialsFromSettings(File settingsFile) {
		if ( ! settingsFile.exists() ) {
			log.warn( "Maven settings.xml file did not exist : " + settingsFile.getAbsolutePath() );
			// EARLY EXIT
			return Collections.emptyMap();
		}

		try {
			final SAXReader saxReader = buildSAXReader();
			final InputSource inputSource = new InputSource( new FileInputStream( settingsFile ) );

			try {
				final List<Element> serverElements = seekServerElements( saxReader.read( inputSource ) );

				if ( serverElements.isEmpty() ) {
					log.warn( "Maven settings.xml file did not contain <sever/> elements : " + settingsFile.getAbsolutePath() );
					// EARLY EXIT
					return Collections.emptyMap();
				}

				final Map<String,Credentials> result = new HashMap<>();

				for ( Element serverElement : serverElements ) {
					final String id = DomHelper.extractValue( serverElement.element( "id" ) );
					if ( id == null ) {
						continue;
					}

					log.debug( "Adding credentials for server : " + id );

					final Credentials authentication = extractCredentials( serverElement );
					if ( authentication != null ) {
						result.put( id, authentication );
					}
				}

				return Collections.unmodifiableMap( result );
			}
			catch (DocumentException e) {
				log.error( "Error reading Maven settings.xml", e );
			}
		}
		catch ( FileNotFoundException e ) {
			log.info( "Unable to locate Maven settings.xml" );
		}

		return Collections.emptyMap();
	}

	private SAXReader buildSAXReader() {
		SAXReader saxReader = new SAXReader( new DocumentFactory() );
		saxReader.setMergeAdjacentText( true );
		return saxReader;
	}

	private Credentials extractCredentials(Element serverElement) {
		final String passwordValue = extractValue( serverElement.element( "password" ) );
		if ( passwordValue == null ) {
			return null;
		}

		final String password = PasswordProcessor.INSTANCE.resolvePasswordStrategy( passwordValue ).interpretPassword( passwordValue );

		final Credentials authentication = new Credentials();
		authentication.setUserName( extractValue( serverElement.element( "username" ) ) );
		authentication.setPassword( password );
		authentication.setPrivateKey( extractValue( serverElement.element( "privateKey" ) ) );
		authentication.setPassphrase( extractValue( serverElement.element( "passphrase" ) ) );

		return authentication;
	}

	private String extractValue(Element element) {
		return valueProcessor.processValue( DomHelper.extractValue( element ) );
	}

	private List<Element> seekServerElements(Document document) {
		final Element settingsElement = document.getRootElement();
		if ( settingsElement != null ) {
			final Element serversElement = settingsElement.element( "servers" );
			if ( serversElement != null ) {
				return serversElement.elements( "server" );
			}
		}

		return Collections.emptyList();
	}

	@Override
	public Credentials determineAuthentication(String repoId) {
		return credentialsByRepoIdMap.get( repoId );
	}
}
