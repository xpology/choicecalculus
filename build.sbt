name in ThisBuild := "choicecalculus"

organization in ThisBuild := "de.unimarburg.ps"

version in ThisBuild := "0.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.10.3"

resolvers in ThisBuild ++= Seq (
    Resolver.sonatypeRepo ("releases"),
    Resolver.sonatypeRepo ("snapshots")
)

libraryDependencies in ThisBuild ++= Seq (
    "com.googlecode.kiama" %% "kiama" % "1.5.2",
    "org.scalatest" %% "scalatest" % "1.9" % "test"
)

scalacOptions in (Compile,doc) := Seq("-groups", "-implicits")

// Has to be false, since multiple tests use global state messaging system
parallelExecution in Test := false

logLevel in ThisBuild := Level.Info

mainClass in ThisBuild := Some("choicecalculus.JsCcCompiler")

/**
 * sbt shell prompt
 */
shellPrompt <<= (name, version) { (_, v) =>
     _ => "choicecalc (" + v + ")> "
}
