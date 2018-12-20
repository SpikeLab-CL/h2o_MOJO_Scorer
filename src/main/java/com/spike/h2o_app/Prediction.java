package com.spike.h2o_app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Prediction {

	String prediction;
	Map<String, String> params = new HashMap<String, String>();

	public Prediction(String result, Map<String, List<String>> params) {
		this.prediction = result;
		Set<String> p = params.keySet();
		for (String param : p) {
			List<String> value = params.get(param);
			this.params.put(param, value.get(0));
		}
	}
	
	public String asJson() {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		String jsonString = gson.toJson(this);
		return jsonString;
	}
}
