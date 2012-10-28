(
  (val do-nothing unit)
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
        do-nothing
        (pyramid (.shiftY (.shrinkIn c 1) 1) m)
      )
    )
  )
  (def house (start-point h w d floor-m walls-m roof-m)
    (let (c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
      (begin
        (cube:set-all   c "air")
        (cube:set-floor c floor-m)
        (cube:set-walls c walls-m)
        (pyramid (.expandOut (.ceiling c) 1) roof-m)
      )
    )
  )
  (defrec house-row (at nr-houses house-builder-f)
    (if (eq nr-houses 0)
      do-nothing
      (begin
        ;(println nr-houses)
        (house-builder-f at)
        (house-row (loc (+ 20 (.getX at)) (.getY at) (.getZ at)) (- nr-houses 1) house-builder-f)
      )
    )
  )

  ; builds a skyscraper
  (def skyscraper (l)   (house l 50 8 10 "stone" "obsidian" "diamond_block"))
  ; builds a house - 5 = wood plank, 17 = wood, 20 = glass
  (def normal-house (l) (house l 4 3 3 "5" "17" "20"))

  (def city () (house-row XYZ 10 skyscraper))
  (def town () (house-row XYZ  6 normal-house))
)