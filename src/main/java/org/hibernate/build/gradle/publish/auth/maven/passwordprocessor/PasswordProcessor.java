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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonatype.plexus.components.cipher.DefaultPlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipher;
import org.sonatype.plexus.components.cipher.PlexusCipherException;

/**
 * Contract for processors of passwords from the Maven {@code settings.xml} file. Main use cases for such processing are
 * the decryption of encrypted passwords and the unescaping of escaped passwords.
 *
 * @author Gunnar Morling
 */
public abstract class PasswordProcessor {

	private static final Logger log = LoggerFactory.getLogger( PasswordProcessor.class );

	protected final String password;
	protected final PlexusCipher cipher;

	protected PasswordProcessor(String password, PlexusCipher cipher) {
		this.password = password;
		this.cipher = cipher;
	}

	/**
	 * Gets a {@link PasswordProcessor} instance for the given password.
	 *
	 * @param password The password to handle.
	 * @return A processor for the given password.
	 */
	public static PasswordProcessor getInstance(String password) {
		PlexusCipher cipher = buildCipher();

		if ( cipher == null || !cipher.isEncryptedString( password ) ) {
			return new DefaultPasswordProcessor( password );
		}
		else {
			return new DecryptionProcessor( password, cipher );
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

	/**
	 * Processes this processor's password.
	 *
	 * @return The processed password. May be the same as the original password but never {@code null}.
	 */
	public abstract String processPassword();
}
