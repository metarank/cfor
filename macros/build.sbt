name := "cfor"

publishMavenStyle := true

publishTo := localStaging.value

licenses := Seq("APL2" -> uri("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(uri("https://github.com/metarank/cfor"))
scmInfo := Some(
  ScmInfo(
    uri("https://github.com/metarank/cfor"),
    "scm:git@github.com:metarank/cfor.git"
  )
)
developers := List(
  Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = uri("https://dfdx.me/"))
)
