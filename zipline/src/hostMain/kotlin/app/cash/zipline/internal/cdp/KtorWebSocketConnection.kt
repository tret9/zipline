@file:OptIn(ExperimentalAtomicApi::class)

package app.cash.zipline.internal.cdp

import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.launch

/**
 * A [CdpClientConnection] over any multiplatform [WebSocketSession]: the
 * Ktor CIO server session on JNI platforms, or a RawWebSocket over
 * ktor-network on Kotlin/Native.
 */
internal class KtorWebSocketClientConnection(
  private val ws: WebSocketSession,
  private val scope: CoroutineScope,
) : CdpClientConnection {
  private val open = AtomicBoolean(true)

  /**
   * Frames in the order they were sent. One coroutine writes them: CDP messages must reach the
   * client in order, e.g. every `HeapProfiler.addHeapSnapshotChunk` before the
   * `takeHeapSnapshot` response.
   */
  private val outgoing = Channel<Frame>(Channel.UNLIMITED)

  init {
    scope.launch {
      for (frame in outgoing) {
        try {
          ws.send(frame)
          ws.flush()
        } catch (_: Throwable) {
          open.store(false)
        }
        if (frame is Frame.Close) break
      }
    }
  }

  override fun sendText(text: String) {
    if (!open.load()) return
    outgoing.trySend(Frame.Text(text))
  }

  fun sendPong(data: ByteArray) {
    outgoing.trySend(Frame.Pong(data))
  }

  override fun isOpen(): Boolean = open.load()

  override fun closeQuietly() {
    if (open.compareAndSet(true, false)) {
      outgoing.trySend(Frame.Close())
      outgoing.close()
    }
  }
}

/**
 * Drives one debugger client over [ws]: registers it with this debug
 * session, forwards text frames to [CdpDebugServer.DebugSession.onCdpMessage],
 * answers pings, and unregisters when the client disconnects.
 */
internal suspend fun CdpDebugServer.DebugSession.serveWebSocket(
  ws: WebSocketSession,
  scope: CoroutineScope,
) {
  val conn = KtorWebSocketClientConnection(ws, scope)
  addClient(conn)
  try {
    for (frame in ws.incoming) {
      when (frame) {
        is Frame.Text -> onCdpMessage(frame.readText())
        is Frame.Ping -> conn.sendPong(frame.data)
        is Frame.Close -> break
        else -> Unit
      }
    }
  } finally {
    removeClient(conn)
    conn.closeQuietly()
  }
}
