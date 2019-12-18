/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.publish.auth.maven;

import java.io.File;
import java.net.URL;

/**
 * @author Steve Ebersole
 */
public class TestHelper {

	// our output is the base for all of the test projects, so we must be
	// able to locate that..  this is our `${buildDir}/resources/test` directory
	// (or IDE equivalent) within that directory we will have access to the Maven
	// `settings.xml` as well as the various test project root dirs
	public static File testProjectsBaseDirectory() {
		final URL baseUrl = TestHelper.class.getResource( "/test_resources_out_dir_locator.properties" );
		return new File( baseUrl.getFile() ).getParentFile();
	}

	public static File projectDirectory(String projectName) {
		return new File( testProjectsBaseDirectory(), "projects/" + projectName );
	}

	public static File settingsXmlFile() {
		return new File( testProjectsBaseDirectory(), "env/settings.xml" );
	}

	public static File securitySettingsXmlFile() {
		return new File( testProjectsBaseDirectory(), "env/settings-security.xml" );
	}

	public static File encryptedSettingsXmlFile() {
		return new File( testProjectsBaseDirectory(), "env/encrypted-settings.xml" );
	}

	public static File environmentSettingsXmlFile() {
		return new File( testProjectsBaseDirectory(), "env/environment-settings.xml" );
	}

	public static File systemPropertiesSettingsXmlFile() {
		return new File( testProjectsBaseDirectory(), "env/system-properties-settings.xml" );
	}
}
