import org.apache.kafka.clients.producer.KafkaProducer
import org.apache.kafka.clients.producer.ProducerRecord
import java.util.Properties

fun main() {
    val props = Properties().apply {
        put("bootstrap.servers", "localhost:9092")
        put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer")
        put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer")
    }

    val producer = KafkaProducer<String, String>(props)

    repeat(1000) { i ->
        val key = "key-$i"

        val value = """
    {
      "eventId": $i,
      "timestamp": ${System.currentTimeMillis()},
      "driverId": "driver-$i",
      "serviceName": "tracking-service",
      "durationMs": ${100 + i}
    }
""".trimIndent()


        producer.send(ProducerRecord("microtrack-events", key, value))
    }

    producer.flush()
    producer.close()
    println("Mensagens enviadas com sucesso!")
}
