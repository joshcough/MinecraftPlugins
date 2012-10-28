(
  (let (c (new jcdc.pluginfactory.Cube XYZ (loc (+ X 10) Y (+ Z 10))))
    (begin
      (cube:set-walls c "stone")
      (cube:set-walls (.expandOut c 1) "brick")
      (cube:set-walls (.expandOut (.expandOut c 1) 1) "gold_block")
      (.expandOut (.expandOut c 1) 1)
    )
  )
)