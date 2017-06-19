name := "tw_rest"

version := "1.0"

scalaVersion := "2.12.1"


libraryDependencies += "org.apache.ignite" % "ignite-core" % "2.0.0"
libraryDependencies += "org.slf4j"        % "slf4j-api"            % "1.7.12"
libraryDependencies += "ch.qos.logback"   % "logback-classic"      % "1.1.3"

libraryDependencies += "com.sparkjava" % "spark-core" % "2.6.0"

// https://mvnrepository.com/artifact/com.google.code.gson/gson
libraryDependencies += "com.google.code.gson" % "gson" % "2.8.1"

// https://mvnrepository.com/artifact/org.apache.ignite/ignite-log4j
libraryDependencies += "org.apache.ignite" % "ignite-log4j" % "2.0.0"

// https://mvnrepository.com/artifact/org.apache.ignite/ignite-indexing
libraryDependencies += "org.apache.ignite" % "ignite-indexing" % "2.0.0"