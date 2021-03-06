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

import com.typesafe.sbt.packager.NativePackagerKeys
import com.typesafe.sbt.packager.archetypes.JavaServerAppKeys
import com.typesafe.sbt.packager.debian.DebianKeys
import com.typesafe.sbt.packager.docker.DockerKeys
import com.typesafe.sbt.packager.universal.UniversalKeys

object Packages extends DockerKeys with JavaServerAppKeys with DebianKeys with UniversalKeys with NativePackagerKeys {

  // Common settings
  val commonSettings = Seq(
    maintainer := "Adrien Mannocci <adrien.mannocci@gmail.com>"
  )

  // Debian settings
  val debianSettings = Seq(
    packageSummary := "High Performance events processing",
    packageDescription := "Transport and process your logs, events, or other data",
    daemonStdoutLogFile := Some("streamy.log")
  )

  // Docker settings
  val dockerSettings = Seq(
    dockerBaseImage := "openjdk:8u151-jre"
  )

  // Package settings
  val settings = commonSettings ++ debianSettings ++ dockerSettings

}
