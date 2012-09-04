package jcdc.pluginfactory

import org.scalacheck.Properties

object ParserCombinatorTests extends Properties("ParserCombinatorTests") with Compare {

  compare("6", 6, 6)

}