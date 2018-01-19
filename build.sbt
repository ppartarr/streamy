/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017-2018
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

import sbt.Keys._

// Disable parallel execution
parallelExecution in ThisBuild := false

lazy val commonSettings = Seq(
  name := "streamy",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := "2.12.4",

  // Scala compiler options
  scalacOptions in(Compile, doc) ++= Seq(
    "-no-link-warnings" // Suppresses problems with Scaladoc @throws links
  )
)

lazy val core = project
  .in(file("core"))
  .settings(commonSettings, Packages.settings, Dependencies.testSettings, Publish.settings)

lazy val bench = project
  .in(file("bench"))
  .dependsOn(
    core % "test->test",
    `plugin-fingerprint` % "test->test",
    `plugin-syslog` % "test->test",
    `plugin-json` % "test->test",
    `plugin-metric` % "test->test",
  )
  .settings(Benchs.settings)
  .enablePlugins(JmhPlugin)

lazy val `plugin-fingerprint` = project
  .in(file("plugin-fingerprint"))
  .settings(commonSettings, Dependencies.testSettings, Publish.settings)
  .dependsOn(core % "provided->compile")

lazy val `plugin-syslog` = project
  .in(file("plugin-syslog"))
  .settings(commonSettings, Dependencies.testSettings, Publish.settings)
  .dependsOn(core % "provided->compile")

lazy val `plugin-json` = project
  .in(file("plugin-json"))
  .settings(commonSettings, Dependencies.testSettings, Publish.settings)
  .dependsOn(core % "provided->compile")

lazy val `plugin-metric` = project
  .in(file("plugin-metric"))
  .settings(commonSettings, Dependencies.testSettings, Publish.settings)
  .dependsOn(core % "provided->compile")

lazy val test = project
  .in(file("test"))
  .settings(commonSettings, Publish.settings)

lazy val root = project
  .settings(Seq(publish := {}))
  .aggregate(core, `plugin-fingerprint`, `plugin-syslog`, `plugin-json`, `plugin-metric`, test)