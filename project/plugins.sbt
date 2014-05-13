resolvers ++= Seq("Bukkit" at "http://repo.bukkit.org/content/repositories/releases")

addSbtPlugin("com.joshcough" % "scala-minecraft-yml-gen" % "0.3.1")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

seq(bintrayResolverSettings:_*)
