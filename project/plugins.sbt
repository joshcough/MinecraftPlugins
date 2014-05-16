resolvers += Resolver.url(
  "Josh Cough sbt plugins",
  url("http://dl.bintray.com/content/joshcough/sbt-plugins"))(
    Resolver.ivyStylePatterns)

resolvers ++= Seq(
  "Bukkit" at "http://repo.bukkit.org/content/repositories/releases"
)

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")

seq(bintrayResolverSettings:_*)
