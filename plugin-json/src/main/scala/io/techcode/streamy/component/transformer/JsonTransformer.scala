/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2017
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
package io.techcode.streamy.component.transformer

import akka.NotUsed
import akka.stream.scaladsl.Flow
import io.techcode.streamy.component.SimpleTransformer
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour
import io.techcode.streamy.component.SimpleTransformer.SuccessBehaviour.SuccessBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour
import io.techcode.streamy.component.Transformer.ErrorBehaviour.ErrorBehaviour
import io.techcode.streamy.component.transformer.JsonTransformer.Mode.Mode
import io.techcode.streamy.component.transformer.JsonTransformer.{Config, Mode}
import io.techcode.streamy.util.json._

/**
  * Json transformer implementation.
  */
private[transformer] class JsonTransformer(config: Config) extends SimpleTransformer(config) {

  // Serialize function
  lazy val serialize: Json => Option[Json] = (value: Json) => Some(value.toString)

  // Deserialize function
  lazy val deserialize: Json => Option[Json] = (value: Json) => value.asString.map { field =>
    // Try to avoid parsing of wrong json
    if (field.startsWith("{") && field.endsWith("}")) {
      // Try to parse
      Json.parse(field) match {
        case Right(succ) => succ
        case Left(ex) => onError(state = value, ex = Some(ex))
      }
    } else {
      onError(state = value)
    }
  }

  // Determine at runtime witch function to use and allow inline
  val function: Json => Option[Json] = config.mode match {
    case Mode.Serialize => serialize
    case Mode.Deserialize => deserialize
  }

  @inline override def transform(value: Json): Option[Json] = function(value)

}

/**
  * Json transform companion.
  */
object JsonTransformer {

  // Component configuration
  case class Config(
    override val source: JsonPointer,
    override val target: Option[JsonPointer] = None,
    override val onSuccess: SuccessBehaviour = SuccessBehaviour.Skip,
    override val onError: ErrorBehaviour = ErrorBehaviour.Skip,
    mode: Mode = Mode.Deserialize
  ) extends SimpleTransformer.Config(source, target, onSuccess, onError)

  // Mode implementation
  object Mode extends Enumeration {
    type Mode = Value
    val Serialize, Deserialize = Value
  }

  /**
    * Create a json transformer flow that transform incoming [[Json]] objects.
    *
    * @param conf flow configuration.
    * @return new json flow.
    */
  def transformer(conf: Config): Flow[Json, Json, NotUsed] =
    Flow.fromFunction(new JsonTransformer(conf))

}