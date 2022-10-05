#!/bin/sh
PROJECT=$HOME/IdeaProjects/elijah-lang
REPOSITORY=$HOME/.m2/repository

$JAVA_HOME/bin/java -classpath $PROJECT/target/classes:$PROJECT/out/test/test:\
$PROJECT/out/production/main:$PROJECT/lib/javassist-3.1/javassist.jar:\
$PROJECT/lib/org.eclipse.jdt.annotation_2.2.200.v20180921-1416.jar:\
$PROJECT/lib/annotations-16.0.2.jar:\
$PROJECT/lib/com/thoughtworks/xstream/xstream/1.4.11.1/xstream-1.4.11.1.jar:\
$PROJECT/lib/xmlpull/xmlpull/1.1.3.1/xmlpull-1.1.3.1.jar:\
$PROJECT/lib/xpp3/xpp3_min/1.1.4c/xpp3_min-1.1.4c.jar:\
$PROJECT/lib/commons-cli-1.4.jar:\
$PROJECT/lib/lombok.jar:\
$REPOSITORY/junit/junit/4.12/junit-4.12.jar:\
$REPOSITORY/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:\
$REPOSITORY/com/google/guava/guava/29.0-jre/guava-29.0-jre.jar:\
$REPOSITORY/com/google/guava/failureaccess/1.0.1/failureaccess-1.0.1.jar:\
$REPOSITORY/com/google/guava/listenablefuture/9999.0-empty-to-avoid-conflict-with-guava/listenablefuture-9999.0-empty-to-avoid-conflict-with-guava.jar:\
$REPOSITORY/com/google/code/findbugs/jsr305/3.0.2/jsr305-3.0.2.jar:\
$REPOSITORY/org/checkerframework/checker-qual/2.11.1/checker-qual-2.11.1.jar:\
$REPOSITORY/com/google/errorprone/error_prone_annotations/2.3.4/error_prone_annotations-2.3.4.jar:\
$REPOSITORY/com/google/j2objc/j2objc-annotations/1.3/j2objc-annotations-1.3.jar:\
$PROJECT/../tripleo-buffers/target/buffers-v1-0.0.2a.jar tripleo.elijah.Main $@
