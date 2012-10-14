package jcdc.pluginfactory.examples

object MineLangExamples {
  val house =
    """
    (
      (def closed (c)
        (or
          (<= (- (cube:max-x c) (cube:min-x c)) 1)
          (<= (- (cube:max-z c) (cube:min-z c)) 1)
        )
      )
      (defrec pyramid (c m)
        (begin
          (cube:set-walls c m)
          (if (closed c)
            unit
            (pyramid (cube:shift-y (cube:shrink-in c 1) 1) m)
          )
        )
      )
      (def house (start-point h w d floor-m walls-m roof-m)
        (let (c (cube:grow-up (cube:expand-z (cube:expand-x (cube start-point start-point) w) d) h))
          (begin
            (cube:set-all   c (material "air"))
            (cube:set-floor c floor-m)
            (cube:set-walls c walls-m)
            (pyramid (cube:expand-out (cube:ceiling c) 1) roof-m)
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
          (<= (- (cube:max-x c) (cube:min-x c)) 1)
          (<= (- (cube:max-z c) (cube:min-z c)) 1)
        )
      )
      (defrec pyramid (c m)
        (begin
          (cube:set-walls c m)
          (if (closed c)
            unit
            (pyramid (cube:shift-y (cube:shrink-in c 1) 1) m)
          )
        )
      )
      (def house (start-point h w d floor-m walls-m roof-m)
        (let (c (cube:grow-up (cube:expand-z (cube:expand-x (cube start-point start-point) w) d) h))
          (begin
            (cube:set-all   c (material "air"))
            (cube:set-floor c floor-m)
            (cube:set-walls c walls-m)
            (pyramid (cube:expand-out (cube:ceiling c) 1) roof-m)
          )
        )
      )
    )
    """.stripMargin.trim

  val expansionTest =
    """
    (
      (let (c (cube XYZ (loc (+ X 10) Y (+ Z 10))))
        (begin
          (cube:set-walls c (material "stone"))
          (cube:set-walls (cube:expand-out c 1) (material "brick"))
          (cube:set-walls (cube:expand-out (cube:expand-out c 1) 1) (material "gold_block"))
          (cube:expand-out (cube:expand-out c 1) 1)
        )
      )
    )
    """.stripMargin
}
