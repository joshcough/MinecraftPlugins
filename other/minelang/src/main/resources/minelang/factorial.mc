(
  (def test []
    (letrec (fact (fn [n] (if (eq? n 0) 1 (* n (fact (- n 1))))))
      (fact 5)
    )
  )

  (defrec factorial [n] (if (eq? n 0) 1 (* n (factorial (- n 1)))))
)