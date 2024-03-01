package android.debug.zenohjavaserver

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.zenoh.Config
import io.zenoh.Session
import io.zenoh.keyexpr.KeyExpr
import io.zenoh.query.Reply
import io.zenoh.sample.Sample
import io.zenoh.selector.Selector
import org.eclipse.uprotocol.v1.UMessage
import java.util.Optional
import java.util.concurrent.BlockingQueue

class ZenohSubber {
    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun periodicallySubFromZenoh() {
            Log.d("ZenohDemo", "entered periodicallyGetFromZenoh()");
            try {
                val jsonConfig = """
                                 {
                                   "mode": "peer",
                                   "connect": { "endpoints": ["unixsock-stream//tmp/zenoh_java_socket"] }
                                 }
                                 """.trimIndent()
                val config: Config = Config.from(jsonConfig)
                Session.open(config).use { session ->
                // Session.open().use { session ->
                    KeyExpr.tryFrom("demo/example/zenoh-java-pub").use { keyExpr ->
                        Log.d("SessionStatus", "Declaring Subscriber on '$keyExpr'...")
                        session.declareSubscriber(keyExpr).res().use { subscriber ->
                            val receiver: BlockingQueue<Optional<Sample>>? = subscriber.receiver
                            checkNotNull(receiver)
                            var idx = 0
                            while (true) {
                                val wrapper: Optional<Sample> = receiver.take()
                                val receiveTime = System.nanoTime()
                                if (wrapper.isEmpty) break
                                val sample: Sample = wrapper.get()
                                val bytes = sample.value.payload
                                val messageRequest = UMessage.parseFrom(bytes)
                                Log.d("ZenohJavaClient", "[${String.format("%4s", idx)}] receiveTime: $receiveTime")
//                                Log.d("Subscriber", ">> Received ${sample.kind} ('${sample.keyExpr}': '${sample.value}' : '$messageRequest')")
                                idx++
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("SessionError", "Error during session or subscriber operation", e)
            }
        }
    }
}