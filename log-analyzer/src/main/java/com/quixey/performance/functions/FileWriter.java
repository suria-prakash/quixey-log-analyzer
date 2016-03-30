package com.quixey.performance.functions;

import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.function.VoidFunction2;
import org.apache.spark.streaming.Time;

import com.quixey.performance.properties.Properties;

public class FileWriter implements VoidFunction2<JavaRDD<String>, Time> {
	private static final long serialVersionUID = 42l;

	public void call(JavaRDD<String> rdd, Time time) throws Exception {
		if (!rdd.partitions().isEmpty()) {
			String path = Properties.getString("quixey.spark.hdfs_output_file") + "_" + time.milliseconds()
					+ "/result.txt";
			rdd.saveAsTextFile(path);
		}
	}
}