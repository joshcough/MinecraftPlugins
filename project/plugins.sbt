resolvers += Resolver.url(
  "Josh Cough sbt plugins",
  url("https://dl.bintray.com/content/joshcough/sbt-plugins"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")

addSbtPlugin("ch.epfl.scala" % "sbt-scala3-migrate" % "0.4.6")