addSbtPlugin("com.joshcough" % "scala-minecraft-yml-gen" % "0.3.1")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.9.1")

seq(bintrayResolverSettings:_*)
