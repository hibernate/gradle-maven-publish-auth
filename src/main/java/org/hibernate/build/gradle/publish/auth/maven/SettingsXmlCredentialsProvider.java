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
package org.hibernate.build.gradle.publish.auth.maven;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentFactory;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.hibernate.build.gradle.publish.auth.maven.passwordprocessor.PasswordProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

/**
 * Provider of credentials based on standard Maven conventions using {@literal settings.xml}.
 *
 * @author Steve Ebersole
 */
public class SettingsXmlCredentialsProvider implements CredentialsProvider {
	private static final Logger log = LoggerFactory.getLogger( SettingsXmlCredentialsProvider.class );

	/**
	 * Singleton access
	 */
	public static final SettingsXmlCredentialsProvider INSTANCE = new SettingsXmlCredentialsProvider();

	public static final String SETTINGS_LOCATION_OVERRIDE = "maven.settings";

	private final ConcurrentHashMap<String,Credentials> credentialsByRepoIdMap;

	private SettingsXmlCredentialsProvider() {
		final File settingsFile = determineSettingsFileLocation();
		this.credentialsByRepoIdMap = extractCredentialsFromSettings( settingsFile );
	}

	private File determineSettingsFileLocation() {
		final String defaultLocation = "~/.m2/settings.xml";
		final String location = System.getProperty( SETTINGS_LOCATION_OVERRIDE, defaultLocation );
		return new File( PathHelper.normalizePath( location ) );
	}

	private ConcurrentHashMap<String, Credentials> extractCredentialsFromSettings(File settingsFile) {
		final ConcurrentHashMap<String, Credentials> byIdMap = new ConcurrentHashMap<String, Credentials>();
		if ( ! settingsFile.exists() ) {
			log.warn( "Maven settings.xml file did not exist : " + settingsFile.getAbsolutePath() );
			// EARLY EXIT
			return byIdMap;
		}

		try {
			SAXReader saxReader = buildSAXReader();
			InputSource inputSource = new InputSource( new FileInputStream( settingsFile ) );
			try {
				final Iterator<Element> serverIterator = seekServerElements( saxReader.read( inputSource ) );
				while ( serverIterator.hasNext() ) {
					final Element serverElement = serverIterator.next();
					final String id = DomHelper.extractValue( serverElement.element( "id" ) );
					if ( id == null ) {
						continue;
					}

					log.debug( "Adding credentials for server : " + id );

					final Credentials authentication = extractCredentials( serverElement );
					byIdMap.put( id, authentication );
				}
			}
			catch (DocumentException e) {
				log.error( "Error reading Maven settings.xml", e );
			}
		}
		catch ( FileNotFoundException e ) {
			log.info( "Unable to locate Maven settings.xml" );
		}

		return byIdMap;
	}

	private SAXReader buildSAXReader() {
		SAXReader saxReader = new SAXReader( new DocumentFactory() );
		saxReader.setMergeAdjacentText( true );
		return saxReader;
	}

	private Credentials extractCredentials(Element serverElement) {
		String passwordValue = DomHelper.extractValue( serverElement.element( "password" ) );
		String password = PasswordProcessor.getInstance( passwordValue ).processPassword();

		final Credentials authentication = new Credentials();
		authentication.setUserName( DomHelper.extractValue( serverElement.element( "username" ) ) );
		authentication.setPassword( password );
		authentication.setPrivateKey( DomHelper.extractValue( serverElement.element( "privateKey" ) ) );
		authentication.setPassphrase( DomHelper.extractValue( serverElement.element( "passphrase" ) ) );
		return authentication;
	}

	@SuppressWarnings("unchecked")
	private Iterator<Element> seekServerElements(Document document) {
		final Element settingsElement = document.getRootElement();
		if ( settingsElement != null ) {
			final Element serversElement = settingsElement.element( "servers" );
			if ( serversElement != null ) {
				return serversElement.elementIterator( "server" );
			}
		}

		return Collections.<Element>emptyList().iterator();
	}

	@Override
	public Credentials determineAuthentication(String repositoryId) {
		return credentialsByRepoIdMap.get( repositoryId );
	}
}
