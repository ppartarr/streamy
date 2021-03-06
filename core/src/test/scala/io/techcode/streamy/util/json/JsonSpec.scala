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
package io.techcode.streamy.util.json

import java.io.ByteArrayInputStream

import akka.util.ByteString
import com.google.common.math.{IntMath, LongMath}
import org.scalatest._

/**
  * Json spec.
  */
class JsonSpec extends WordSpecLike with Matchers {

  "Json object" should {
    "equals JsObject independently of field order" in {
      Json.obj(
        "field1" -> 123,
        "field2" -> "beta",
        "field3" -> Json.obj(
          "field31" -> true,
          "field32" -> 123.45,
          "field33" -> Json.arr("blabla", 456L, JsNull)
        )
      ) should equal(Json.obj(
        "field2" -> "beta",
        "field3" -> Json.obj(
          "field31" -> true,
          "field33" -> Json.arr("blabla", 456L, JsNull),
          "field32" -> 123.45
        ),
        "field1" -> 123))
    }

    "not be equals when there is a deep difference" in {
      Json.obj(
        "field1" -> 123,
        "field2" -> "beta",
        "field3" -> Json.obj(
          "field31" -> true,
          "field32" -> 123.45,
          "field33" -> Json.arr("blabla", JsNull)
        )
      ) should not equal Json.obj(
        "field2" -> "beta",
        "field3" -> Json.obj(
          "field31" -> true,
          "field33" -> Json.arr("blabla", 456L),
          "field32" -> 123.45
        ),
        "field1" -> 123)
    }

    "be create from builder" in {
      val builder = Json.objectBuilder()
      builder.put("test" -> "test")
      builder.remove("test")
      builder.put("foobar" -> "test")
      builder.putAll(Json.objectBuilder().put("foobar" -> "test"))
      builder.result()
      builder.put("foobar" -> "notModified")
      builder.putAll(Json.objectBuilder().put("foobar" -> "notModified"))
      builder.result() should equal(Json.obj("foobar" -> "test"))
      builder.contains("foobar") should equal(true)
      builder.contains("notPresent") should equal(false)
      builder.get("foobar") should equal(Some(JsString("test")))
    }

    "be create empty" in {
      Json.obj().eq(Json.obj()) should equal(true)
    }

    "be iterate using a foreach" in {
      var founded = false
      Json.obj("test" -> "test").foreach(el => founded |= el._2.equals(JsString("test")))
      founded should equal(true)
    }

    "return field set" in {
      Json.obj("test" -> "test").fieldSet should equal(Set("test" -> JsString("test")))
    }

    "return values as iterable" in {
      Json.obj("test" -> "test").values.head should equal(Seq(JsString("test")).head)
    }

    "not be equals when there is a difference" in {
      Json.obj(
        "field1" -> 123,
        "field2" -> "beta",
        "field3" -> Json.obj(
          "field31" -> true,
          "field32" -> 123.45,
          "field33" -> Json.arr("blabla", 456L, JsNull)
        )
      ) should not equal Json.obj(
        "field3" -> Json.obj(
          "field31" -> true,
          "field33" -> Json.arr("blabla", 456L, JsNull),
          "field32" -> 123.45
        ),
        "field1" -> 123)
    }

    "be update by adding a unique tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.put("add" -> "foobar")
      result should equal(Json.obj(
        "test" -> "foobar",
        "add" -> "foobar"
      ))
      result should not equal input
    }

    "be update by adding a tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.put("test" -> "updated")
      result should equal(Json.obj("test" -> "updated"))
      result should not equal input
    }

    "be update by removing an existing tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.remove("test")
      result should equal(Json.obj())
      result should not equal input
    }

    "be update by removing a tuple" in {
      val input = Json.obj("test" -> "foobar")
      val result = input.remove("foobar")
      result should equal(Json.obj("test" -> "foobar"))
    }

    "be merge with another json object" in {
      val input = Json.obj("test" -> "foobar")
      val toMerge = Json.obj("foobar" -> "test")
      val result = input.merge(toMerge)
      result should equal(Json.obj(
        "test" -> "foobar",
        "foobar" -> "test"
      ))
      result should not equal input
    }

    "be merge with another empty json object" in {
      val input = Json.obj("test" -> "foobar")
      val toMerge = Json.obj()
      val result = input.merge(toMerge)
      result should equal(Json.obj("test" -> "foobar"))
    }

    "return value if present" in {
      val input = Json.obj("test" -> "foobar")
      input("test") should equal(Some(JsString("foobar")))
    }

    "return none if absent" in {
      val input = Json.obj("test" -> "foobar")
      input("missing") should equal(None)
    }

    "not fail to deep merge when the objects are empty" in {
      Json.obj().deepMerge(Json.obj()) should equal(Json.obj())
    }

    "deep merge correctly when the source object is empty" in {
      def populatedObj = Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull
      )

      populatedObj.deepMerge(Json.obj()) should equal(populatedObj)
    }

    "deep merge correctly when the incoming object is empty" in {
      val populatedObj = Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull
      )

      Json.obj().deepMerge(populatedObj) should equal(populatedObj)
    }

    "should keep existing attributes where there is no collision and overwrite existing attributes on collision when value is not a JsArray or JsObject" in {
      Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull,
        "field4" -> 456,
        "field5" -> "abc",
        "field6" -> "def"
      ).deepMerge(Json.obj(
        "field4" -> 789,
        "field5" -> "xyz",
        "field6" -> JsNull
      )) should equal(Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> JsNull,
        "field4" -> 789,
        "field5" -> "xyz",
        "field6" -> JsNull
      ))
    }

    "should keep existing attributes where there is no collision and recursively merge where elements are both of type JsArray or both of type JsObject" in {
      Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> Json.arr(
          "abc", "def", "ghi"
        ),
        "field4" -> Json.obj(
          "field1a" -> 888,
          "field2b" -> "xxx",
          "field3c" -> JsNull
        )
      ).deepMerge(Json.obj(
        "field3" -> Json.arr(
          "jkl", "mno", "pqr"
        ),
        "field4" -> Json.obj(
          "field1a" -> 999,
          "field2b" -> "yyy",
          "field3c" -> "zzz"
        )
      )) should equal(Json.obj(
        "field1" -> 123,
        "field2" -> "abc",
        "field3" -> Json.arr(
          "jkl", "mno", "pqr"
        ),
        "field4" -> Json.obj(
          "field1a" -> 999,
          "field2b" -> "yyy",
          "field3c" -> "zzz"
        )
      ))
    }

    "should keep existing attributes where there is no collision and properly merge a deep structure" in {
      Json.obj(
        "field1a" -> Json.obj(
          "field2a" -> Json.obj(
            "field3a" -> Json.obj(
              "field4a" -> Json.obj(
                "field5a" -> "abc",
                "field5b" -> Json.arr("111", "222"),
                "field5d" -> Json.arr(Json.obj("a" -> 1), Json.obj("b" -> 2))
              )
            ),
            "field2b" -> Json.arr("aaa", "bbb"),
            "field2c" -> Json.obj(
              "hello" -> "world"
            )
          ),
          "field2b" -> "xxx",
          "field2c" -> JsNull
        )
      ).deepMerge(Json.obj(
        "field1a" -> Json.obj(
          "field2a" -> Json.obj(
            "field3a" -> Json.obj(
              "field4a" -> Json.obj(
                "field5b" -> Json.arr("333", "444"),
                "field5c" -> "deep",
                "field5d" -> Json.arr(Json.obj("c" -> 3), Json.obj("d" -> 4))
              )
            ),
            "field2b" -> Json.arr("ccc", "ddd"),
            "field2c" -> Json.obj(
              "hello" -> "new world"
            )
          ),
          "field2b" -> "yyy",
          "field2d" -> "zzz"
        )
      )) should equal(Json.obj(
        "field1a" -> Json.obj(
          "field2a" -> Json.obj(
            "field3a" -> Json.obj(
              "field4a" -> Json.obj(
                "field5a" -> "abc",
                "field5b" -> Json.arr("333", "444"),
                "field5c" -> "deep",
                "field5d" -> Json.arr(Json.obj("c" -> 3), Json.obj("d" -> 4))
              )
            ),
            "field2b" -> Json.arr("ccc", "ddd"),
            "field2c" -> Json.obj(
              "hello" -> "new world"
            )
          ),
          "field2b" -> "yyy",
          "field2c" -> JsNull,
          "field2d" -> "zzz"
        )
      ))
    }

    "return correct size" in {
      Json.obj("test" -> "test").sizeHint should equal(15)
    }
  }

  "Json array" should {
    "return value if present" in {
      val input = Json.arr("test", "foobar")
      input(1) should equal(Some(JsString("foobar")))
    }

    "return none if absent" in {
      val input = Json.arr("test", "foobar")
      input(2) should equal(None)
    }

    "be create from builder" in {
      val builder = Json.arrayBuilder()
      builder.add("test")
      builder.remove()
      builder.add("foobar")
      builder.addAll(Json.arrayBuilder().add("foobar"))
      builder.result()
      builder.remove()
      builder.add("notModified")
      builder.addAll(Json.arrayBuilder().add("notModified"))
      builder.result() should equal(Json.arr("foobar", "foobar"))
    }

    "be create empty" in {
      Json.arr().eq(Json.arr()) should equal(true)
    }

    "return head of json array if present" in {
      val input = Json.arr("test", "foobar")
      input.head() should equal(Some(JsString("test")))
    }

    "return head of json array if not present" in {
      val input = Json.arr()
      input.head() should equal(None)
    }

    "return last of json array if present" in {
      val input = Json.arr("test", "foobar")
      input.last() should equal(Some(JsString("foobar")))
    }

    "return last of json array if not present" in {
      val input = Json.arr()
      input.last() should equal(None)
    }

    "append json array correctly" in {
      val input = Json.arr("test01", "test02")
      input.append(Json.arr("test03")) should equal(Json.arr("test01", "test02", "test03"))
    }

    "append json value correctly" in {
      val input = Json.arr("test01", "test02")
      input.append("test03") should equal(Json.arr("test01", "test02", "test03"))
    }

    "prepend json array correctly" in {
      val input = Json.arr("test01", "test02")
      input.prepend(Json.arr("test03")) should equal(Json.arr("test03", "test01", "test02"))
    }

    "prepend json value correctly" in {
      val input = Json.arr("test01", "test02")
      input.prepend("test03") should equal(Json.arr("test03", "test01", "test02"))
    }

    "return correct size" in {
      Json.arr("test", 2, Json.obj("test" -> "test"), 4.0).sizeHint should equal(30) // ["test",2,{"test":"test"},4.0]
    }
  }

  "Json" should {
    "stringify long integers correctly" in {
      val input = Json.obj("l" -> 1330950829160L)
      input.toString should equal("""{"l":1330950829160}""")
    }

    "stringify short integers correctly" in {
      val s: Short = 1234
      val input = Json.obj("s" -> s)
      input.toString should equal("""{"s":1234}""")
    }

    "stringify byte integers correctly" in {
      val b: Byte = 123
      val input = Json.obj("b" -> b)
      input.toString should equal("""{"b":123}""")
    }

    "stringify boolean correctly" in {
      JsTrue.toString should equal("true")
    }

    "stringify float correctly" in {
      JsFloat(1.0F).toString should equal("1.0")
    }

    "stringify double correctly" in {
      JsDouble(1.0D).toString should equal("1.0")
    }

    "stringify null correctly" in {
      JsNull.toString should equal("null")
    }

    "stringify bytestring correctly" in {
      JsBytes(ByteString("test")).toString should equal("\"dGVzdA==\"")
    }

    "stringify big decimal correctly" in {
      val n = BigDecimal("12345678901234567890.42")
      val input = Json.obj("bd" -> n)
      input.toString should equal("""{"bd":12345678901234567890.42}""")
    }

    "stringify big decimal with large exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.2e1000"))
      input.toString should equal("""{"bd":1.2E+1000}""")
    }

    "stringify big decimal with large negative exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("6.75e-1000"))
      input.toString should equal("""{"bd":6.75E-1000}""")
    }

    "stringify big decimal with small exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.234e3"))
      input.toString should equal("""{"bd":1234}""")
    }

    "stringify big decimal with small negative exponents in scientific notation correctly" in {
      val input = Json.obj("bd" -> BigDecimal("1.234e-3"))
      input.toString should equal("""{"bd":0.001234}""")
    }

    "stringify big decimal with integer base correctly" in {
      val input = Json.obj("bd" -> BigDecimal("2e128"))
      input.toString should equal("""{"bd":2E+128}""")
    }

    "stringify list correctly" in {
      val input = Json.arr("123", 123, BigDecimal("2e128"))
      input.toString should equal("""["123",123,2E+128]""")
    }

    "parse long integers correctly" in {
      val input = Json.parse("1330950829160").getOrElse(JsNull)
      input should equal(JsLong(1330950829160L))
    }

    "parse short integers correctly" in {
      val input = Json.parse("1234").getOrElse(JsNull)
      input should equal(JsInt(1234))
    }

    "parse byte integers correctly" in {
      val input = Json.parse("123").getOrElse(JsNull)
      input should equal(JsInt(123))
    }

    "parse big decimal correctly" in {
      val input = Json.parse("12345678901234567890.42").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("12345678901234567890.42")))
    }

    "parse big decimal with large exponents in scientific notation correctly" in {
      val input = Json.parse("1.2e1000").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("1.2e1000")))
    }

    "parse big decimal with large negative exponents in scientific notation correctly" in {
      val input = Json.parse("6.75e-1000").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("6.75e-1000")))
    }

    "parse big decimal with small exponents in scientific notation correctly" in {
      val input = Json.parse("1.234e3").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("1.234e3")))
    }

    "parse big decimal with small negative exponents in scientific notation correctly" in {
      val input = Json.parse("1.234e-3").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("1.234e-3")))
    }

    "parse big decimal with integer base correctly" in {
      val input = Json.parse("2e128").getOrElse(JsNull)
      input should equal(JsBigDecimal(BigDecimal("2e128")))
    }

    "parse list correctly" in {
      val input = Json.parse("""["123",123,2E+128]""").getOrElse(JsNull)
      input should equal(Json.arr("123", 123, BigDecimal("2e128")))
    }

    "parse null values in object" in {
      val input = Json.parse("""{"foo": null}""").getOrElse(JsNull)
      input should equal(Json.obj("foo" -> JsNull))
    }

    "parse null values in array" in {
      val input = Json.parse("""[null]""").getOrElse(JsNull)
      input should equal(Json.arr(JsNull))
    }

    "parse null as JsNull" in {
      val input = Json.parse("""null""").getOrElse(JsNull)
      input should equal(JsNull)
    }

    "parse json object from bytes" in {
      val input = Json.parse("""{"test":"test"}""".getBytes).getOrElse(JsNull)
      input should equal(Json.obj("test" -> "test"))
    }

    "handle parsing failure from bytes" in {
      val input = Json.parse("""test:"test"""".getBytes).getOrElse(JsNull)
      input should equal(JsNull)
    }

    "parse json object from bytestring" in {
      val input = Json.parse(ByteString("""{"test":"test"}""")).getOrElse(JsNull)
      input should equal(Json.obj("test" -> "test"))
    }

    "handle parsing failure from bytestring" in {
      val input = Json.parse(ByteString("""test:"test"""")).getOrElse(JsNull)
      input should equal(JsNull)
    }

    "be convert to json object when possible" in {
      Json.obj().asObject should equal(Some(Json.obj()))
    }

    "fail to be convert to json object when not possible" in {
      JsString("10").asObject should equal(None)
    }

    "can be convert to iterator" in {
      Json.arr("foobar").toIterator.next() should equal(JsString("foobar"))
    }

    "can be convert to seq" in {
      Json.arr("foobar").toSeq.head should equal(JsString("foobar"))
    }

    "be convert to json array when possible" in {
      Json.arr().asArray should equal(Some(Json.arr()))
    }

    "fail to be convert to json array when not possible" in {
      JsString("10").asArray should equal(None)
    }

    "be convert to boolean when possible" in {
      JsTrue.asBoolean should equal(Some(true))
    }

    "fail to be convert to boolean when not possible" in {
      JsString("10").asBoolean should equal(None)
    }

    "return correct size for boolean" in {
      JsTrue.sizeHint should equal(4)
      JsFalse.sizeHint should equal(5)
    }

    "be convert to string when possible" in {
      JsString("10").asString should equal(Some("10"))
    }

    "fail to be convert to string when not possible" in {
      JsTrue.asString should equal(None)
    }

    "return correct size for string" in {
      JsString("test").sizeHint should equal(6) // "test"
    }

    "be convert to big decimal when possible" in {
      JsBigDecimal(BigDecimal("1e20")).asBigDecimal should equal(Some(BigDecimal("1e20")))
    }

    "fail to be convert to big decimal when not possible" in {
      JsTrue.asBigDecimal should equal(None)
    }

    "return correct size for big decimal" in {
      JsBigDecimal(BigDecimal("2e128")).sizeHint should equal(6)
    }

    "return int conversion for big decimal" in {
      JsBigDecimal(BigDecimal(6)).toInt should equal(6)
    }

    "return long conversion for big decimal" in {
      JsBigDecimal(BigDecimal(6L)).toLong should equal(6L)
    }

    "return float conversion for big decimal" in {
      JsBigDecimal(BigDecimal(6.0F)).toFloat should equal(6.0F)
    }

    "return double conversion for big decimal" in {
      JsBigDecimal(BigDecimal(6.0D)).toDouble should equal(6.0D)
    }

    "return big decimal conversion for big decimal" in {
      JsBigDecimal(BigDecimal("2e128")).toBigDecimal should equal(BigDecimal("2e128"))
    }

    "be convert to null when possible" in {
      JsNull.asNull should equal(Some(()))
    }

    "fail to be convert to null when not possible" in {
      JsTrue.asNull should equal(None)
    }

    "return correct size for null" in {
      JsNull.sizeHint should equal(4)
    }

    "be convert to int when possible" in {
      JsInt(1).asInt should equal(Some(1))
    }

    "fail to be convert to int when not possible" in {
      JsTrue.asInt should equal(None)
    }

    "return correct size for int" in {
      // Positive cases
      var size = 1
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        JsInt(1 * IntMath.pow(10, i)).sizeHint should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Int.MaxValue).length) {
        JsInt(-1 * IntMath.pow(10, i)).sizeHint should equal(size)
        size += 1
      }
    }

    "return int conversion for int" in {
      JsInt(1).toInt should equal(1)
    }

    "return long conversion for int" in {
      JsInt(1).toLong should equal(1L)
    }

    "return float conversion for int" in {
      JsInt(1).toFloat should equal(1.0F)
    }

    "return double conversion for int" in {
      JsInt(1).toDouble should equal(1.0D)
    }

    "return big decimal conversion for int" in {
      JsInt(1).toBigDecimal should equal(BigDecimal(1))
    }

    "be convert to long when possible" in {
      JsLong(1).asLong should equal(Some(1))
    }

    "fail to be convert to long when not possible" in {
      JsTrue.asLong should equal(None)
    }

    "return correct size for long" in {
      // Positive cases
      var size = 1
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        JsLong(1 * LongMath.pow(10, i)).sizeHint should equal(size)
        size += 1
      }

      // Negative cases
      size = 2
      for (i <- 0 until String.valueOf(Long.MaxValue).length) {
        JsLong(-1 * LongMath.pow(10, i)).sizeHint should equal(size)
        size += 1
      }
    }

    "return int conversion for long" in {
      JsLong(1L).toInt should equal(1)
    }

    "return long conversion for long" in {
      JsLong(1L).toLong should equal(1L)
    }

    "return float conversion for long" in {
      JsLong(1L).toFloat should equal(1.0F)
    }

    "return double conversion for long" in {
      JsLong(1L).toDouble should equal(1.0D)
    }

    "return big decimal conversion for long" in {
      JsLong(1L).toBigDecimal should equal(BigDecimal(1))
    }

    "be convert to float when possible" in {
      JsFloat(1.0F).asFloat should equal(Some(1.0F))
    }

    "fail to be convert to float when not possible" in {
      JsTrue.asFloat should equal(None)
    }

    "return correct size for float" in {
      JsFloat(2.0F).sizeHint should equal(3)
    }

    "return int conversion for float" in {
      JsFloat(2.0F).toInt should equal(2)
    }

    "return long conversion for float" in {
      JsFloat(2.0F).toLong should equal(2L)
    }

    "return float conversion for float" in {
      JsFloat(2.0F).toFloat should equal(2.0F)
    }

    "return double conversion for float" in {
      JsFloat(2.0F).toDouble should equal(2.0D)
    }

    "return big decimal conversion for float" in {
      JsFloat(2.0F).toBigDecimal should equal(BigDecimal(2.0F))
    }

    "be convert to double when possible" in {
      JsDouble(1.0D).asDouble should equal(Some(1.0D))
    }

    "fail to be convert to double when not possible" in {
      JsTrue.asDouble should equal(None)
    }

    "return correct size for double" in {
      JsDouble(2.0D).sizeHint should equal(3)
    }

    "return int conversion for double" in {
      JsDouble(2.0D).toInt should equal(2)
    }

    "return long conversion for double" in {
      JsDouble(2.0D).toLong should equal(2L)
    }

    "return float conversion for double" in {
      JsDouble(2.0D).toFloat should equal(2.0F)
    }

    "return double conversion for double" in {
      JsDouble(2.0D).toDouble should equal(2.0D)
    }

    "return big decimal conversion for double" in {
      JsDouble(2.0D).toBigDecimal should equal(BigDecimal(2.0D))
    }

    "be convert to number when possible" in {
      JsDouble(1.0D).asNumber should equal(Some(JsDouble(1.0D)))
    }

    "fail to be convert to number when not possible" in {
      JsTrue.asNumber should equal(None)
    }

    "be convert to bytes when possible" in {
      JsBytes(ByteString("test")).asBytes should equal(Some(ByteString("test")))
    }

    "fail to be convert to bytes when not possible" in {
      JsTrue.asBytes should equal(None)
    }

    "handle patch with operations" in {
      val input = Json.obj()
      val result = input.patch(
        Add(Root / "foobar", "foobar"),
        Add(Root / "test", "test")
      )
      result should equal(Some(Json.obj(
        "foobar" -> "foobar",
        "test" -> "test"
      )))
    }

    "handle patch with seq operations" in {
      val input = Json.obj("foobar" -> "foobar")
      val result = input.patch(Seq(
        Remove(Root / "foobar" / "test" / "test", mustExist = false),
        Replace(Root / "foobar", "test")
      ))
      result should equal(Some(Json.obj(
        "foobar" -> "test"
      )))
    }
  }

}