(; program to build houses and cities
  (val do-nothing unit)
  (def even (n) (eq (% n 2) 0))

  (defrec pyramid (c m)
    (begin
      (cube:set-walls c m)
      (let (closed (lam (c)
                      (or
                        (<= (- (.maxX c) (.minX c)) 1)
                        (<= (- (.maxZ c) (.minZ c)) 1)
                      )
                    ))
        (if (closed c)
          do-nothing
          (pyramid (.shiftY (.shrinkIn c 1) 1) m)
        )
      )
    )
  )
  (def build-ceiling (c m) (begin (pyramid (.expandOut (.ceiling c) 1) m) c))
  (def build-house (start-point h w d floor-m walls-m roof-m)
    (let (c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
      (begin
        (cube:set-all   c "air")
        (cube:set-floor c floor-m)
        (cube:set-walls c walls-m)
        (build-ceiling c roof-m)
        c
      )
    )
  )
  (defrec build-house-row (at nr-houses house-builder-f)
    (if (eq nr-houses 0)
      do-nothing
      (begin
        ;(message nr-houses)
        (house-builder-f at)
        (build-house-row (loc (+ 20 (.getX at)) (.getY at) (.getZ at)) (- nr-houses 1) house-builder-f)
      )
    )
  )
  ; builds a skyscraper
  (def build-skyscraper (l)   (build-house l 50 8 10 "stone" "obsidian" "diamond_block"))
  ; builds a house - 5 = wood plank, 17 = wood, 20 = glass
  (def build-normal-house (l) (build-house l 4 3 3 "5" "17" "20"))

  (def city    () (build-house-row XYZ 10 build-skyscraper))
  (def village () (build-house-row XYZ  6 build-normal-house))

  (def living-house ()
    (let (c (normal-house XYZ))
      (spawn 100 1 (lam (n) (cube:set-walls c (if (even n) "gold_block" "gold_ore"))))
    )
  )
)