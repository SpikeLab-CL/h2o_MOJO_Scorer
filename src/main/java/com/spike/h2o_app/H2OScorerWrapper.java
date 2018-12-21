package com.spike.h2o_app;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hex.ModelCategory;
import hex.genmodel.MojoModel;
import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.exception.PredictException;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.ClusteringModelPrediction;
import hex.genmodel.easy.prediction.RegressionModelPrediction;

public class H2OScorerWrapper {

	private String modelPath;
	private MojoModel model;
	private EasyPredictModelWrapper predictModelWrapper;
	private ModelCategory modelCategory;

	public H2OScorerWrapper(String modelPath) throws IOException {
		try {
			this.modelPath = modelPath;
			this.model = MojoModel.load(this.modelPath);
			predictModelWrapper = new EasyPredictModelWrapper(model);
			modelCategory = predictModelWrapper.getModelCategory();

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String predict(Map<String, List<String>> params) throws PredictException {
		
		double prediction = 0;
		Set<String> features = params.keySet();
		RowData data = new RowData();
		
		for (String feature : features) {
			List<String> value = params.get(feature);
			data.put(feature, value.get(0));
		}
		
		if(modelCategory.toString().equals("Regression")) {
			RegressionModelPrediction p = predictModelWrapper.predictRegression(data);
			prediction = p.value;
			return Double.toString(prediction);
		}
		
		else if(modelCategory.toString().equals("Binomial")) {
			BinomialModelPrediction p = predictModelWrapper.predictBinomial(data);
			String label = p.label;
			return label;
		}
		else if(modelCategory.toString().equals("Clustering")) {
			ClusteringModelPrediction p = predictModelWrapper.predictClustering(data);
			int cluster = p.cluster;
			return Integer.toString(cluster);
		}
		else {
			return "model type not supported";
		}
		//TODO: add Clustering
	}
}
