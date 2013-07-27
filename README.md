Version 2.0
===========

Gradle plugin facilitating publishing to authenticated Maven repositories without adding credential information
directly in the build file or in random properties files.  Support for applying authentication information from
Maven's settings.xml file is provided out-of-the-box.


Applying plugin
---------------

To apply the plugin, simply specify:

    apply plugin: 'maven-publish-auth'

The plugin class is org.hibernate.build.gradle.publish.auth.maven.AuthenticationManager, whose purpose is really just
to create an instance each of
* org.hibernate.build.gradle.publish.auth.maven.LegacyAuthenticationHandler
* org.hibernate.build.gradle.publish.auth.maven.PublishingAuthenticationHandler

These handlers get attached to tasks of type org.gradle.api.tasks.Upload and
org.gradle.api.publish.maven.tasks.PublishToMavenRepository, respectively.

But in either case, both ultimately delegate to any org.hibernate.build.gradle.publish.auth.maven.CredentialsProvider
instances configured with the org.hibernate.build.gradle.publish.auth.maven.CredentialsProviderRegistry.  Currently
there is only one supported implementation, org.hibernate.build.gradle.publish.auth.maven.SettingsXmlCredentialsProvider
which parses the Maven settings.xml file and makes those authentication credentials available to Gradle.  This
includes support for Maven "encrypted" passwords as outlined at
http://maven.apache.org/guides/mini/guide-encryption.html.

By default, settings.xml is loaded from its default location (~/.m2/settings.xml).  You can alter this by specifying
`-Dmaven.settings=your/path`.


Support for the new Gradle Publication DSL
------------------------------------------

One of the new (and still ongoing and incubating) developments in Gradle is the new Publication DSL as the means for
declaring how your project artifacts are published.  See the Gradle UserGuide for more information.

The "bridge" between Maven authentication credentials and Gradle is the repository id.  For an example, lets assume
you have a Maven settings.xml that defines the following credentials:

    <settings>
        <servers>
            <server>
                <id>dogdeball-repo</id>
                <username>average-joes</username>
                <password>dodgethis</password>
             </server>
        </servers>
    </settings>

As you can see, we have credentials associated with a repository server with the identifier of
"dogdeball-repo".  This is the information we need supply in order for the plugin to make the
connection.  In the Publication DSL, this corelates to the `name` attribute of the repository configuration:

    publishing {
        repositories {
            maven {
                name = 'dogdeball-repo'
                url 'http://repository.average.joes.com'
            }
        }
    }



Support for legacy Gradle publishing (Upload task)
--------------------------------------------------

If you prefer to stick with the legacy publishing DSL, I have left support of that in place for the time being too.
Again, the bridge is the repository id.

    uploadArchives {
        repositories.mavenDeployer {
            ...
            repository(id: 'dogdeball-repo', url: 'http://repository.average.joes.com')
        }
    }

Support for downloading artifacts
----------------------------------

Plugin also allows to download artifacts from authenticated Maven repositories without adding credentials directly
in build file. Again, the bridge is the repository id.

    repositories {
        maven {
                name = 'dogdeball-repo'
                url 'http://repository.average.joes.com'
        }
    }

Currently plugin does not apply authentication information to buildscript repositories, so following won't work:

    buildscript {
        repositories {
            maven {
                name = 'dogdeball-repo'
                url 'http://repository.average.joes.com'
            }
        }
    }
