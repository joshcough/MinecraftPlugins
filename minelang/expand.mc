(; simple, fun floor expansion program
  (let (c (new jcdc.pluginfactory.Cube XYZ (loc (+ X 10) Y (+ Z 10))))
    (begin
      (cube:set-walls c "stone")
      (cube:set-walls (.expandOut c 1) "brick")
      (cube:set-walls (.expandOut (.expandOut c 1) 1) "gold_block")

      ; an different way to do the same thing above
      ; not quite as nice, but more functional
      (cube:set-walls
        (.expandOut
          (cube:set-walls
            (.expandOut
              (cube:set-walls c "stone")
              1
            )
            "brick"
          )
          1
        )
        "gold_block"
      )
    )
  )
)