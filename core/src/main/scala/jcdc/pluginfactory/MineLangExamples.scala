package jcdc.pluginfactory

object MineLangExamples {

  val expansionTest =
    """
    (
      (let (c (new jcdc.pluginfactory.Cube XYZ (loc (+ X 10) Y (+ Z 10))))
        (begin
          (cube:set-walls c (material "stone"))
          (cube:set-walls (.expandOut c 1) (material "brick"))
          (cube:set-walls (.expandOut (.expandOut c 1) 1) (material "gold_block"))
          (.expandOut (.expandOut c 1) 1)
        )
      )
    )
    """.stripMargin
}
