addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")

resolvers ++= Seq("Bukkit" at "http://repo.bukkit.org/content/repositories/releases")

resolvers += Resolver.jcenterRepo

resolvers += Resolver.url(
  "bintray-sbt-plugin-releases",
  url("http://dl.bintray.com/content/sbt/sbt-plugin-releases"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("com.joshcough" % "minecraft-sbt-plugin" % "0.3.5")