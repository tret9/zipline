package app.cash.zipline

import app.cash.zipline.hermes.HermesBridge_createHandle
import app.cash.zipline.hermes.HermesBridge_freeHandle
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.cinterop.COpaquePointer
import kotlinx.cinterop.ExperimentalForeignApi

/**
 * Runtime tests for bridgeForAny and JsNumberToLong against a real Hermes context.
 */
@OptIn(ExperimentalForeignApi::class)
class BridgeForAnyRuntimeTest {
  private val engine = JsEngine.create()

  @AfterTest
  fun tearDown() {
    engine.close()
  }

  /** Evaluates [expression] and passes a bridge handle to its value to [block]. */
  private fun <T> withHandle(expression: String, block: (COpaquePointer?, Int) -> T): T {
    engine.evaluate("globalThis.__bridgeForAnyTestValue = ($expression);")
    val context = engine.contextPointer
    // Parent handle 0 reads from the global object.
    val handle = HermesBridge_createHandle(context, 0, "__bridgeForAnyTestValue")
    try {
      return block(context, handle)
    } finally {
      HermesBridge_freeHandle(context, handle)
    }
  }

  @Test
  fun `JsNumberToLong handles JS int`() {
    assertEquals(42L, withHandle("42", ::JsNumberToLong))
  }

  @Test
  fun `JsNumberToLong handles KotlinJS Long object`() {
    assertEquals(1L, withHandle("({low_1: 1, high_1: 0})", ::JsNumberToLong))
  }

  @Test
  fun `JsNumberToLong handles negative KotlinJS Long`() {
    assertEquals(-1L, withHandle("({low_1: -1, high_1: -1})", ::JsNumberToLong))
  }

  @Test
  fun `bridgeForAny returns number as Double`() {
    assertEquals(42.0, withHandle("42", ::bridgeForAny))
  }

  @Test
  fun `bridgeForAny returns String`() {
    assertEquals("hello", withHandle("'hello'", ::bridgeForAny))
  }

  @Test
  fun `bridgeForAny returns Boolean`() {
    assertEquals(true, withHandle("true", ::bridgeForAny))
  }

  @Test
  fun `bridgeForAny returns null for undefined`() {
    assertNull(withHandle("undefined", ::bridgeForAny))
  }

  @Test
  fun `bridgeForAny returns array as List`() {
    assertEquals(listOf(1.0, "two", true), withHandle("[1, 'two', true]", ::bridgeForAny))
  }
}
