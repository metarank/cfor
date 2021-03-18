name := "cfor"

publishMavenStyle := true

publishTo := sonatypePublishToBundle.value

licenses := Seq("APL2" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

homepage := Some(url("https://github.com/metarank/cfor"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/metarank/cfor"),
    "scm:git@github.com:metarank/cfor.git"
  )
)
developers := List(
  Developer(id = "romangrebennikov", name = "Roman Grebennikov", email = "grv@dfdx.me", url = url("https://dfdx.me/"))
)
