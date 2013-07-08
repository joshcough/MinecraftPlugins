cd ..
echo sbt "project $1" "run-main jcdc.pluginfactory.YMLGenerator jcdc.pluginfactory.examples.$1 JoshCough 0.3.1 examples/$1" package
sbt "project $1" "run-main jcdc.pluginfactory.YMLGenerator jcdc.pluginfactory.examples.$1 JoshCough 0.3.1 examples/$1" package
cd examples/$1
echo cp target/scala-2.10/$1_2.10-0.3.1.jar "$BUKKIT/plugins/"
cp target/scala-2.10/$1_2.10-0.3.1.jar "$BUKKIT/plugins/"
cp target/scala-2.10/$1_2.10-0.3.1.jar "$BUKKIT/plugins/"
