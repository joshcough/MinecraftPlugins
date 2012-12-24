(
  (def and [a b] (if a b false))
  (def or  [a b] (if a true b))
  (def zero? [x] (if (eq? x 0) true false))
  (def not   [x] (if x 1 0))
)