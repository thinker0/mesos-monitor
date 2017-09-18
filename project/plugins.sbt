logLevel := Level.Info

resolvers += "Typesafe repository" at "http://dl.bintray.com/typesafe/maven-releases/"

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.4")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.8.0")
