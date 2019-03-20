package io.enkrypt.kafka.streams.processors

import io.enkrypt.avro.processing.BlockAuthorRecord
import io.enkrypt.kafka.streams.config.Topics.CanonicalBlockAuthors
import io.enkrypt.kafka.streams.config.Topics.CanonicalBlocks
import io.enkrypt.kafka.streams.utils.toTopic
import mu.KLogger
import mu.KotlinLogging
import org.apache.kafka.streams.StreamsBuilder
import org.apache.kafka.streams.StreamsConfig
import org.apache.kafka.streams.Topology
import java.util.Properties

class BlockAuthorProcessor : AbstractKafkaProcessor() {

  override val id: String = "block-author-processor"

  override val kafkaProps: Properties = Properties()
    .apply {
      putAll(baseKafkaProps.toMap())
      put(StreamsConfig.APPLICATION_ID_CONFIG, id)
      put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, 1)
    }

  override val logger: KLogger = KotlinLogging.logger {}

  override fun buildTopology(): Topology {

    // Create stream builder
    val builder = StreamsBuilder().apply {}

    CanonicalBlocks.stream(builder)
      .mapValues { v ->
        when (v) {
          null -> null
          else -> BlockAuthorRecord.newBuilder()
            .setAuthor(v.getAuthor())
            .setBlockNumber(v.getNumber())
            .setBlockHash(v.getHash())
            .build()
        }
      }.toTopic(CanonicalBlockAuthors)

    return builder.build()
  }
}
