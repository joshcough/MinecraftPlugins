(
  ; http://stackoverflow.com/questions/6578615/how-to-use-scala-collection-immutable-list-in-a-java-code
  (val empty scala.collection.immutable.Nil$/MODULE$)
  (def cons   [h t] (.apply scala.collection.immutable.$colon$colon$/MODULE$ h t))

  ; all these should stay the same even if i change the underlying representation above
  ; but if i had pattern matching they could all be much nicer
  (def empty? [l]   (or (eq? l empty) (eq? l false)))
  (def head   [l]   (.head l))
  (def tail   [l]   (.tail l))
  (def list?  [l]   (isa? l scala.collection.immutable.List))

  (defrec append [l1 l2] (if (empty? l1) l2 (cons (head l1) (append (tail l1) l2))))
  (val join append)
  (def unit      [a] (cons a empty))
  ;(a -> b -> a) -> a -> [b] -> a
  (defrec foldl  [f init l] (if (empty? l) init (foldl f (f init (head l)) (tail l))))
  ;(a -> b -> b) -> b -> [a] -> b
  (defrec foldr  [f init l] (if (empty? l) init (f (head l) (foldr f init (tail l)))))
  (def    bind   [f xs] (foldr (fn [a b] (join (f a) b)) empty xs))
  (defrec fmap   [f xs] (bind  (fn [x]   (unit (f x))) xs))
  (defrec filter [p xs] (foldr (fn [a b] (if (p a) (cons a b) b)) empty xs))
)
