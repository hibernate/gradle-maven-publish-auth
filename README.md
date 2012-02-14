Gradle plugin to allow uploading to authenticated repositories without adding credential information directly in
the build file or in random properties files.  Really this should be part of Gradle's Maven plugin.  The Gradle team
agree at least in principle.  I think its just a matter of them finding time to migrate this code in there.  Vote up
http://issues.gradle.org/browse/GRADLE-2053 if you think this is important.

The plugin class is org.hibernate.build.gradle.upload.UploadAuthenticationManager, whose purpose is really just to
create an instance of the org.hibernate.build.gradle.upload.AuthenticationHandler and attach it to each
org.gradle.api.tasks.Upload task.  The org.hibernate.build.gradle.upload.AuthenticationHandler uses all the
org.hibernate.build.gradle.upload.AuthenticationProvider registered with the
org.hibernate.build.gradle.upload.AuthenticationProviderRegistry until it finds one that understand the
org.apache.maven.artifact.ant.Authentication needed for the org.apache.maven.artifact.ant.RemoteRepository
associated with the org.gradle.api.tasks.Upload task.

Currently the only org.apache.maven.artifact.ant.Authentication is the
org.hibernate.build.gradle.upload.StandardMavenAuthenticationProvider implementation which knows how to
locate authentication information for the org.apache.maven.artifact.ant.RemoteRepository using the Maven
settings.xml file.  By default the current users settings.xml (~/.m2/settings.xml) is used, although this
can be changed by specifying -Dmaven.settings=your/path.

Starting with version 1.1.0, can also handle Maven "encrypted" passwords as outlined at
http://maven.apache.org/guides/mini/guide-encryption.html.

