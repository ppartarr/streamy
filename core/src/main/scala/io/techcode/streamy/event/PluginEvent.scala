/*
 * The MIT License (MIT)
 * <p>
 * Copyright (c) 2018
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
package io.techcode.streamy.event

import akka.actor.DeadLetterSuppression

/**
  * Represent a app event.
  */
abstract class PluginEvent(val name: String) extends DeadLetterSuppression

/**
  * Represent an plugin loading event.
  * This event is fired when a plugin is loading.
  */
case class LoadingPluginEvent(override val name: String) extends PluginEvent(name)

/**
  * Represent an plugin running event.
  * This event is fired when a plugin is running.
  */
case class RunningPluginEvent(override val name: String) extends PluginEvent(name)

/**
  * Represent an plugin stopping event.
  * This event is fired when a plugin is stopping.
  */
case class StoppingPluginEvent(override val name: String) extends PluginEvent(name)

/**
  * Represent an plugin stopping event.
  * This event is fired when a plugin is stopped.
  */
case class StoppedPluginEvent(override val name: String) extends PluginEvent(name)
