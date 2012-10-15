package jcdc.pluginfactory

object MineLangExamples {
  val house =
    """
    (
      (def closed (c)
        (or
          (<= (- (.maxX c) (.minX c)) 1)
          (<= (- (.maxZ c) (.minZ c)) 1)
        )
      )
      (defrec pyramid (c m)
        (begin
          (cube:set-walls c m)
          (if (closed c)
            unit
            (pyramid (.shiftY (.shrinkIn c 1) 1) m)
          )
        )
      )
      (def house (start-point h w d floor-m walls-m roof-m)
        (let (c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
          (begin
            (cube:set-all   c (material "air"))
            (cube:set-floor c floor-m)
            (cube:set-walls c walls-m)
            (pyramid (.expandOut (.ceiling c) 1) roof-m)
          )
        )
      )
      (house XYZ 50 8 10 (material "stone") (material "obsidian") (material "diamond_block"))
    )
    """.stripMargin.trim

  val houseDefs =
    """
    (
      (def closed (c)
        (or
          (<= (- (.maxX c) (.minX c)) 1)
          (<= (- (.maxZ c) (.minZ c)) 1)
        )
      )
      (defrec pyramid (c m)
        (begin
          (cube:set-walls c m)
          (if (closed c)
            unit
            (pyramid (.shiftY (.shrinkIn c 1) 1) m)
          )
        )
      )
      (def house (start-point h w d floor-m walls-m roof-m)
        (let (c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
          (begin
            (cube:set-all   c (material "air"))
            (cube:set-floor c floor-m)
            (cube:set-walls c walls-m)
            (pyramid (.expandOut (.ceiling c) 1) roof-m)
          )
        )
      )
    )
    """.stripMargin.trim

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
