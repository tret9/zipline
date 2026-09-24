package app.cash.zipline

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Runtime tests for the JNI bridge dispatch (via JsEngine.evaluate() and evaluateForBridge()).
 * These exercise the same Context::toJavaObject path that the generated C bridges use.
 */
class BridgeForAnyJvmTest {
  private val engine = JsEngine.create()

  @AfterTest
  fun tearDown() {
    engine.close()
  }

  @Test
  fun `evaluate returns Int`() {
    assertEquals(1, engine.evaluate("1"))
  }

  @Test
  fun `evaluate returns Double`() {
    assertEquals(3.14, engine.evaluate("3.14"))
  }

  @Test
  fun `evaluate returns Boolean`() {
    assertEquals(true, engine.evaluate("true"))
    assertEquals(false, engine.evaluate("false"))
  }

  @Test
  fun `evaluate returns String`() {
    assertEquals("hello", engine.evaluate("'hello'"))
  }

  @Test
  fun `evaluate returns null for undefined`() {
    assertNull(engine.evaluate("undefined"))
  }

  @Test
  fun `evaluate returns null for null`() {
    assertNull(engine.evaluate("null"))
  }

  @Test
  fun `evaluate returns array`() {
    val result = engine.evaluateForBridge("[1, 'two', true]", "test.js") as Array<*>
    assertEquals(3, result.size)
    assertEquals(1, result[0])
    assertEquals("two", result[1])
    assertEquals(true, result[2])
  }
}
