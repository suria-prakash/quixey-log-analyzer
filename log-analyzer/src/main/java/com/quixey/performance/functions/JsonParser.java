package com.quixey.performance.functions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.spark.api.java.function.FlatMapFunction;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quixey.performance.model.TraceLogModel;

import scala.Tuple3;

public class JsonParser implements FlatMapFunction<String, Tuple3<Long, String, TraceLogModel>> {

	private static final long serialVersionUID = 8685135406455017478L;

	@Override
	public Iterable<Tuple3<Long, String, TraceLogModel>> call(String jsonLog) throws Exception {
		final ObjectMapper mapper = new ObjectMapper();
		Map<String, Long> startTimeMap = new HashMap<>();
		Map<Long, Map<String, TraceLogModel>> timeTakenMap = new HashMap<>();
		String searchQuery = null;

		try {
			JsonNode root = mapper.readValue(jsonLog, JsonNode.class);
			JsonNode trace = root.get("trace");
			JsonNode annotations = trace.get("annotations");

			// annotations have all the time ranges and of data type array,
			// parsing it

			for (JsonNode eachAnnotation : annotations) {
				TraceLogModel traceLogModel = null;
				JsonNode custom = null;
				long logSeconds;
				Map<String, TraceLogModel> traceLogMap = null;

				switch (eachAnnotation.get("type").textValue()) {
				case "SR":
					startTimeMap.put("SR", Long.parseLong(eachAnnotation.get("timestamp").textValue()));
					custom = eachAnnotation.get("custom");
					searchQuery = custom.get("httpRequestPath").textValue(); 
							//+ "?" + custom.get("httpRequestQuery").textValue();
					break;
				case "SS":
					traceLogModel = new TraceLogModel();
					traceLogModel.setStartTime(startTimeMap.get("SR"));
					traceLogModel.setStopTime(Long.parseLong(eachAnnotation.get("timestamp").textValue()));
					traceLogModel.setRequestProcessingTime(traceLogModel.getStopTime() - traceLogModel.getStartTime());
					traceLogModel.setTypeAndSequence("Complete Search Query");
					traceLogModel.setRequestURI(searchQuery);
					custom = eachAnnotation.get("custom");
					traceLogModel.setHttpStatusCode(custom.get("httpStatusCode").asText());
					if ("null".equals(traceLogModel.getHttpStatusCode())) {
						traceLogModel.setExceptionMessage(custom.get("exceptionMessage").asText());
					}
					logSeconds = traceLogModel.getStartTime() / 1000;
					traceLogMap = timeTakenMap.get(logSeconds);
					if (null == traceLogMap) {
						traceLogMap = new HashMap<>();
						timeTakenMap.put(logSeconds, traceLogMap);
					}
					traceLogMap.put(traceLogModel.getRequestURI(), traceLogModel);
					break;
				case "CS":
					startTimeMap.put("CS_" + eachAnnotation.get("sequence").textValue(),
							Long.parseLong(eachAnnotation.get("timestamp").textValue()));
					break;
				case "CR":
					traceLogModel = new TraceLogModel();
					traceLogModel.setStartTime(startTimeMap.get("CS_" + eachAnnotation.get("sequence").textValue()));
					traceLogModel.setStopTime(Long.parseLong(eachAnnotation.get("timestamp").textValue()));
					traceLogModel.setRequestProcessingTime(traceLogModel.getStopTime() - traceLogModel.getStartTime());
					traceLogModel.setTypeAndSequence("CS/CR " + eachAnnotation.get("sequence").textValue());
					custom = eachAnnotation.get("custom");
					traceLogModel.setRequestURI(custom.get("serviceUri").textValue());
					traceLogModel.setHttpStatusCode(custom.get("httpStatusCode").asText());
					if ("null".equals(traceLogModel.getHttpStatusCode())) {
						traceLogModel.setExceptionMessage(custom.get("exceptionMessage").asText());
					}
					logSeconds = traceLogModel.getStartTime() / 1000;
					traceLogMap = timeTakenMap.get(logSeconds);
					if (null == traceLogMap) {
						traceLogMap = new HashMap<>();
						timeTakenMap.put(logSeconds, traceLogMap);
					}
					traceLogMap.put(traceLogModel.getRequestURI(), traceLogModel);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		List<Tuple3<Long, String, TraceLogModel>> resultantList = new ArrayList<>();
		for (Entry<Long, Map<String, TraceLogModel>> timeTakenLog : timeTakenMap.entrySet()) {
			Map<String, TraceLogModel> eachMap = timeTakenLog.getValue();
			for (Entry<String, TraceLogModel> traceLogEntry : eachMap.entrySet()) {
				resultantList.add(new Tuple3<Long, String, TraceLogModel>(timeTakenLog.getKey(), traceLogEntry.getKey(),
						traceLogEntry.getValue()));
			}
		}

		return resultantList;
	}

}
