package com.quixey.performance.functions;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.VoidFunction;

import com.quixey.performance.model.TimeTakenModel;
import com.quixey.performance.properties.Properties;

import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import scala.Tuple2;

public class KafkaWriter implements Function<JavaPairRDD<Long, Map<String, TimeTakenModel>>, Void> {

	private static final long serialVersionUID = 9021895054170216860L;

	@Override
	public Void call(JavaPairRDD<Long, Map<String, TimeTakenModel>> rdd) throws Exception {
		rdd.foreachPartition(new VoidFunction<Iterator<Tuple2<Long, Map<String, TimeTakenModel>>>>() {

			private static final long serialVersionUID = -7898122292387721425L;
			private final String OUTPUT_KAFKA_TOPIC = Properties.getString("quixey.spark.output.kafka_topic");

			@Override
			public void call(Iterator<Tuple2<Long, Map<String, TimeTakenModel>>> tuples) throws Exception {

				java.util.Properties properties = new java.util.Properties();
				properties.put("metadata.broker.list", "localhost:9092");
				properties.put("serializer.class", "kafka.serializer.StringEncoder");
				properties.put("client.id", "traceLogAnalysisGroup");
				ProducerConfig producerConfig = new ProducerConfig(properties);
				Producer<String, String> producer = new kafka.javaapi.producer.Producer<String, String>(producerConfig);
				KeyedMessage<String, String> message = null;

				for (; tuples.hasNext();) {
					Tuple2<Long, Map<String, TimeTakenModel>> eachTuple = tuples.next();
					for (Entry<String, TimeTakenModel> eachEntry : eachTuple._2.entrySet()) {
						String actualMessage = eachTuple._1 + "," + eachEntry.getKey() + ","
								+ eachEntry.getValue().getMinTimeTaken() + "," + eachEntry.getValue().getMaxTimeTaken()
								+ "," + eachEntry.getValue().getNumSamples() + ","
								+ eachEntry.getValue().getTotalTimeTaken();
						message = new KeyedMessage<String, String>(OUTPUT_KAFKA_TOPIC, actualMessage);
						producer.send(message);
					}
				}
			}
		});
		return null;
	}
}
