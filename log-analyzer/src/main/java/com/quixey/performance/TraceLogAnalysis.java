package com.quixey.performance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.function.FlatMapFunction;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.Function2;
import org.apache.spark.api.java.function.PairFunction;
import org.apache.spark.streaming.Duration;
import org.apache.spark.streaming.api.java.JavaDStream;
import org.apache.spark.streaming.api.java.JavaPairDStream;
import org.apache.spark.streaming.api.java.JavaPairReceiverInputDStream;
import org.apache.spark.streaming.api.java.JavaStreamingContext;
import org.apache.spark.streaming.kafka.KafkaUtils;

import com.quixey.performance.functions.JsonParser;
import com.quixey.performance.functions.KafkaWriter;
import com.quixey.performance.model.TimeTakenModel;
import com.quixey.performance.model.TraceLogModel;
import com.quixey.performance.properties.Properties;

import scala.Tuple2;
import scala.Tuple3;
import scala.Tuple5;

public class TraceLogAnalysis {

	private final Logger LOG = Logger.getLogger(this.getClass());
	private static final String INPUT_KAFKA_TOPIC = Properties.getString("quixey.spark.input.kafka_topic");
	private static final int KAFKA_PARALLELIZATION = Properties.getInt("quixey.spark.kafka_parallelization");

	public static void main(String[] args) {
		BasicConfigurator.configure();
		SparkConf conf = new SparkConf().setAppName("Quixey Trace Log Analysis");

		if (args.length > 0)
			conf.setMaster(args[0]);
		else
			conf.setMaster("local[*]");

		JavaStreamingContext ssc = new JavaStreamingContext(conf, new Duration(2000)); // 2
																						// seconds
																						// window

		Map<String, Integer> topicMap = new HashMap<String, Integer>();
		topicMap.put(INPUT_KAFKA_TOPIC, KAFKA_PARALLELIZATION);

		JavaPairReceiverInputDStream<String, String> messages = KafkaUtils.createStream(ssc,
				Properties.getString("quixey.spark.zkhosts"), "traceLogAnalysisGroup", topicMap);

		JavaDStream<String> jsonLogMessages = messages.map(new Function<Tuple2<String, String>, String>() {
			private static final long serialVersionUID = 23L;

			public String call(Tuple2<String, String> message) {
				return message._2();
			}
		});

		JavaDStream<Tuple3<Long, String, TraceLogModel>> parsedLogs = jsonLogMessages.flatMap(new JsonParser());

		JavaPairDStream<Long, Map<String, TimeTakenModel>> mappedTuples = parsedLogs
				.mapToPair(new PairFunction<Tuple3<Long, String, TraceLogModel>, Long, Map<String, TimeTakenModel>>() {

					private static final long serialVersionUID = -7984629082605271807L;

					@Override
					public Tuple2<Long, Map<String, TimeTakenModel>> call(
							Tuple3<Long, String, TraceLogModel> inputTuple) throws Exception {
						Map<String, TimeTakenModel> timeTakenMap = new HashMap<>();
						long timeTaken = inputTuple._3().getRequestProcessingTime();
						TimeTakenModel timeTakenModel = new TimeTakenModel(1, timeTaken, timeTaken, timeTaken);
						timeTakenMap.put(inputTuple._2(), timeTakenModel);
						return new Tuple2<Long, Map<String, TimeTakenModel>>(inputTuple._1(), timeTakenMap);
					}
				});

		JavaPairDStream<Long, Map<String, TimeTakenModel>> reduceBySecond = mappedTuples.reduceByKey(
				new Function2<Map<String, TimeTakenModel>, Map<String, TimeTakenModel>, Map<String, TimeTakenModel>>() {

					private static final long serialVersionUID = 6L;

					@Override
					public Map<String, TimeTakenModel> call(Map<String, TimeTakenModel> v1,
							Map<String, TimeTakenModel> v2) throws Exception {
						for (Entry<String, TimeTakenModel> eachEntry : v1.entrySet()) {
							String key = eachEntry.getKey();
							if (v2.containsKey(key)) {
								TimeTakenModel timeTakenModel = v2.get(key);
								timeTakenModel.setNumSamples(
										timeTakenModel.getNumSamples() + eachEntry.getValue().getNumSamples());
								timeTakenModel.setTotalTimeTaken(
										timeTakenModel.getTotalTimeTaken() + eachEntry.getValue().getTotalTimeTaken());
								timeTakenModel.setMinTimeTaken(Math.min(timeTakenModel.getMinTimeTaken(),
										eachEntry.getValue().getMinTimeTaken()));
								timeTakenModel.setMaxTimeTaken(Math.max(timeTakenModel.getMaxTimeTaken(),
										eachEntry.getValue().getMaxTimeTaken()));
								v2.put(key, timeTakenModel);
							} else { // not available in v2
								v2.put(key, eachEntry.getValue());
							}
						}
						return v2;
					}
				});

		reduceBySecond.foreachRDD(new KafkaWriter());

//		JavaDStream<Tuple5<Long, String, String, String, String>> toPrintRDD = reduceBySecond.flatMap(
//				new FlatMapFunction<Tuple2<Long, Map<String, TimeTakenModel>>, Tuple5<Long, String, String, String, String>>() {
//
//					private static final long serialVersionUID = 6120729297416787567L;
//
//					@Override
//					public Iterable<Tuple5<Long, String, String, String, String>> call(
//							Tuple2<Long, Map<String, TimeTakenModel>> v1) throws Exception {
//						Map<String, TimeTakenModel> timeTakenModelMap = v1._2;
//						List<Tuple5<Long, String, String, String, String>> listToBeReturned = new ArrayList<>();
//						for (Entry<String, TimeTakenModel> eachEntry : timeTakenModelMap.entrySet()) {
//							TimeTakenModel timeTakenModel = eachEntry.getValue();
//							listToBeReturned.add(new Tuple5<Long, String, String, String, String>(v1._1,
//									" Request URI = " + eachEntry.getKey(),
//									" Total Number of Requests = " + timeTakenModel.getNumSamples(),
//									" Total Time Taken = " + timeTakenModel.getTotalTimeTaken(),
//									" Average Time Taken for Each Request = "
//											+ (float) timeTakenModel.getTotalTimeTaken()
//													/ timeTakenModel.getNumSamples()));
//						}
//						return listToBeReturned;
//					}
//				});

		// toPrintRDD.foreachRDD(new TimeTakenLogPersister());

		ssc.start();
		ssc.awaitTermination();
	}

}
