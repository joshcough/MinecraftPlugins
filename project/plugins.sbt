resolvers += Resolver.url(
  "Josh Cough sbt plugins",
  url("http://dl.bintray.com/content/joshcough/sbt-plugins"))(
    Resolver.ivyStylePatterns)

resolvers ++= Seq(
  "Bukkit" at "http://repo.bukkit.org/content/groups/public/"
)

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
