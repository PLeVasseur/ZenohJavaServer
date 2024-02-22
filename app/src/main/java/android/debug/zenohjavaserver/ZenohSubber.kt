package android.debug.zenohjavaserver

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import io.zenoh.Session
import io.zenoh.keyexpr.KeyExpr
import io.zenoh.query.Reply
import io.zenoh.sample.Sample
import io.zenoh.selector.Selector
import java.util.Optional
import java.util.concurrent.BlockingQueue

class ZenohSubber {
    companion object {
        @RequiresApi(Build.VERSION_CODES.TIRAMISU)
        fun periodicallySubFromZenoh() {
            Log.d("ZenohDemo", "entered periodicallyGetFromZenoh()");
            try {
                Session.open().use { session ->
                    KeyExpr.tryFrom("demo/example/**").use { keyExpr ->
                        Log.d("SessionStatus", "Declaring Subscriber on '$keyExpr'...")
                        session.declareSubscriber(keyExpr).res().use { subscriber ->
                            val receiver: BlockingQueue<Optional<Sample>>? = subscriber.receiver
                            checkNotNull(receiver)
                            while (true) {
                                val wrapper: Optional<Sample> = receiver.take()
                                if (wrapper.isEmpty) break
                                val sample: Sample = wrapper.get()
                                Log.d("Subscriber", ">> Received ${sample.kind} ('${sample.keyExpr}': '${sample.value}')")
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