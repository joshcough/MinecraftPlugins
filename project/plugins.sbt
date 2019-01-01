resolvers += Resolver.url(
  "Josh Cough sbt plugins",
  url("http://dl.bintray.com/content/joshcough/sbt-plugins"))(
    Resolver.ivyStylePatterns)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.9")