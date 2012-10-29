(; program to build houses and cities
  (val do-nothing unit)
  (def even (n) (eq (% n 2) 0))

  ; build a pyramid!
  (defrec pyramid (c m)
    (begin
      (cube:set-walls c m)
      (let (closed (lam (c) (or (<= (- (.maxX c) (.minX c)) 1) (<= (- (.maxZ c) (.minZ c)) 1))))
        (unless (closed c) (pyramid (.shiftY (.shrinkIn c 1) 1) m))
      )
    )
  )
  ; build a single house
  (def build-house (start-point h w d floor-m walls-m roof-m)
    (let* ((c (.growUp (.expandZ (.expandX (new jcdc.pluginfactory.Cube start-point start-point) w) d) h))
           (build-ceiling (lam (c m) (begin (pyramid (.expandOut (.ceiling c) 1) m) c))))
      (begin
        (build-ceiling (cube:set-walls (cube:set-floor (cube:set-all c "air") floor-m) walls-m) roof-m)
        c
      )
    )
  )
  ; build a row of houses
  (defrec build-house-row (at nr-houses house-builder-f)
    (unless (eq nr-houses 0)
      (begin
        ;(message nr-houses)
        (house-builder-f at)
        ; TODO: 20 isnt right here. the houses could be bigger than 20 wide...
        (build-house-row (loc (+ 20 (.getX at)) (.getY at) (.getZ at)) (- nr-houses 1) house-builder-f)
      )
    )
  )
  ; builds a skyscraper
  (def build-skyscraper (l)   (build-house l 50 8 10 "stone" "obsidian" "diamond_block"))
  ; builds a house - 5 = wood plank, 17 = wood, 20 = glass
  (def build-normal-house (l) (build-house l 4 3 3 "5" "17" "20"))

  ; builds a row of skyscrapers. not really a full city, yet.
  (def city    () (build-house-row XYZ 10 build-skyscraper))
  ; builds a row of little houses. could be considered a village.
  (def village () (build-house-row XYZ  6 build-normal-house))

  ; build a house, and then change the wall its walls every second for 100 seconds.
  (def living-house ()
    (let (c (normal-house XYZ))
      (spawn 100 1 (lam (n) (cube:set-walls c (if (even n) "gold_block" "gold_ore"))))
    )
  )
)