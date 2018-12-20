package com.spike.h2o_app;

import java.util.List;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FileUtils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.javalin.Context;
import io.javalin.Javalin;
import javalinjwt.JWTGenerator;
import javalinjwt.JWTProvider;
import javalinjwt.JavalinJWT;
import javalinjwt.examples.JWTResponse;

public class Main {

	public static HashMap<String, String> models = new HashMap<String, String>();
	public static List<User> users = new ArrayList<User>();
	public final static String SECRET = "secret_phare";

	public static void main(String[] args) {

		// JWT
		Algorithm algorithm = Algorithm.HMAC256(SECRET);
		JWTGenerator<User> generator = (user, alg) -> {
			JWTCreator.Builder token = JWT.create().withClaim("name", user.username);
			return token.sign(alg);
		};
		JWTVerifier verifier = JWT.require(algorithm).build();
		JWTProvider provider = new JWTProvider(algorithm, generator, verifier);

		final String cwd = new File("").getAbsolutePath();
		Javalin app = Javalin.create().enableCorsForAllOrigins().start(8080);

		// We add some admin user
		users.add(new User("admin", "pass", true));

		app.post("/model/:model/predict", ctx -> {
			if (validToken(ctx, provider)) {
				String modelName = ctx.pathParam("model");
				String modelPath = models.get(modelName);
				if (modelPath != null) {
					H2OScorerWrapper scorer = new H2OScorerWrapper(modelPath);
					String result = scorer.predict(ctx.formParamMap());
					Prediction prediction = new Prediction(result, ctx.formParamMap());
					ctx.result(prediction.asJson());
				} else {
					JsonObject response = new JsonObject();
					response.addProperty("status", "error");
					response.addProperty("message", "model not found");
					ctx.result(response.toString());
				}
			} else {
				ctx.result("Invalid credentials!");
			}

		});

		app.put("/upload/:model", ctx -> {
			if (validToken(ctx, provider)) {
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
			} else {
				ctx.result("Invalid credentials!");
			}
		});

		app.get("/models/list", ctx -> {
			if (validToken(ctx, provider)) {
				JsonObject response = new JsonObject();
				response.addProperty("op", "available models");
				JsonArray modelList = new JsonArray();
				Iterator m = models.entrySet().iterator();
				while (m.hasNext()) {
					Map.Entry pair = (Map.Entry) m.next();
					JsonObject model = new JsonObject();
					model.addProperty("model_name", pair.getKey().toString());
					modelList.add(model);
				}
				response.add("model_list", modelList);
				ctx.result(response.toString());
			} else {
				ctx.result("Invalid credentials!");
			}

		});

		app.post("/users/get_token", ctx -> {
			try {
				String username = ctx.formParamMap().get("username").get(0);
				String password = ctx.formParamMap().get("password").get(0);
				User user = validateUser(username, password);
				if (user != null) {
					String token = provider.generateToken(user);
					ctx.json(new JWTResponse(token));
				} else {
					ctx.result("Invalid credentials!");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		});
		app.get("/", ctx -> {
			ctx.result("ok");
		});
	}

	public static Boolean validToken(Context context, JWTProvider provider) {
		Optional<DecodedJWT> decodedJWT = JavalinJWT.getTokenFromHeader(context).flatMap(provider::validateToken);
		return decodedJWT.isPresent();
	}

	public static User validateUser(String username, String password) {
		Iterator<User> iterator = users.iterator();
		while (iterator.hasNext()) {
			User user = iterator.next();
			if (user.getUsername().equals(username) && user.getPassword().equals(password)) {
				return user;
			}
		}
		return null;
	}
}
