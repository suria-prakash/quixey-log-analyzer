package com.quixey.performance.functions;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Time;

import com.quixey.performance.properties.Properties;

import scala.Tuple5;

public class TimeTakenLogPersister implements VoidFunction2<JavaRDD<Tuple5<Long, String, String, String, String>>, Time> {
	private static final long serialVersionUID = 42l;

	@Override
	public void call(JavaRDD<Tuple5<Long, String, String, String, String>> rdd, Time time) throws Exception {
		if (!rdd.isEmpty()) {
			String path = Properties.getString("quixey.spark.hdfs_output_file") + "_" + time.milliseconds();
			rdd.saveAsTextFile(path);
		}		
	}
}