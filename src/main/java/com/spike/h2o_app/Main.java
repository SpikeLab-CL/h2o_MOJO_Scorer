package com.spike.h2o_app;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.javalin.Javalin;

public class Main {

	public static HashMap<String, String> models = new HashMap<String, String>();

	public static void main(String[] args) {

		final String cwd = new File("").getAbsolutePath();
		Javalin app = Javalin.create().enableCorsForAllOrigins().start(8080);

		app.post("/model/:model/predict", ctx -> {
			String modelName = ctx.pathParam("model");
			String modelPath = models.get(modelName);
			if(modelPath != null) {
				H2OScorerWrapper scorer = new H2OScorerWrapper(modelPath);
				String result = scorer.predict(ctx.formParamMap());
				Prediction prediction = new Prediction(result, ctx.formParamMap());
				ctx.result(prediction.asJson());				
			}else{
				JsonObject response = new JsonObject();
				response.addProperty("status", "error");
				response.addProperty("message", "model not found");
				ctx.result(response.toString());
			}
		});

		app.put("/upload/:model", ctx -> {
			String modelName = ctx.pathParam("model");
			ctx.uploadedFiles("model").forEach(file -> {
				try {
					String modelPath = cwd + "/models/" + file.getName();
					FileUtils.copyInputStreamToFile(file.getContent(), new File(modelPath));
					models.put(modelName, modelPath);
					ctx.result("Model: " + modelName + " uploaded sucessfully!!");
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
		});

		app.get("/models/list", ctx -> {
			JsonObject response = new JsonObject();
			response.addProperty("op", "available models");
			JsonArray modelList = new JsonArray();
			Iterator m = models.entrySet().iterator();
			while(m.hasNext()) {
		        Map.Entry pair = (Map.Entry)m.next();
		        JsonObject model = new JsonObject();
		        model.addProperty("model_name",pair.getKey().toString());
		        modelList.add(model);
			}
			response.add("model_list", modelList);
			ctx.result(response.toString());
		});
		
		app.get("/", ctx -> {
			ctx.result("Welcome h2o user!");
		});
	}
}
