resolvers += Resolver.url(
  "Josh Cough sbt plugins",
  url("https://dl.bintray.com/content/joshcough/sbt-plugins"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "1.1.0")

addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.4.6")