package regression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Map;

import be.abeel.util.Pair;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.NearestMeanClassifier;
import net.sf.javaml.classification.bayes.NaiveBayesClassifier;
import net.sf.javaml.classification.evaluation.EvaluateDataset;
import net.sf.javaml.classification.evaluation.PerformanceMeasure;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.Instance;
import net.sf.javaml.core.SparseInstance;
import net.sf.javaml.filter.normalize.NormalizeMidrange;
import net.sf.javaml.sampling.Sampling;
import net.sf.javaml.tools.data.FileHandler;

public class BayesJavaML {

	private Dataset createDataset(String matrixFilename, int numFeatures) throws Exception{
		BufferedReader reader = new BufferedReader(new FileReader(matrixFilename));
		String line;
		Instance instance;
		int maxCoord = -1;
		Dataset data = new DefaultDataset();
		int numLines = 0;
		while ((line = reader.readLine()) != null && numLines < 10000) {
			String[] components = line.split(" ");
			if(components.length % 2 != 1) {
				System.out.println("malformed matrix:");
				System.out.println(line);
				break;
			}
			
			
			int label = Integer.parseInt(components[0]);
			if(label == 1) {
				instance = new SparseInstance(numFeatures, "answered");
			} else if (label== 0) {
				instance = new SparseInstance(numFeatures, "unanswered");
			} else {
				System.out.println("bad label: " + label);
				break;
			}
			
			
			int coord = 0;
			for(int i = 1; i < components.length; i++) {
				if(i % 2 == 1) {
					coord = Integer.parseInt(components[i]);
					if(coord > maxCoord) {
						maxCoord = coord;
					}
				} else {
					instance.put(coord, Double.parseDouble(components[i]));
				}
			}
			data.add(instance);
			numLines += 1;
		}
		System.out.println(maxCoord);
		return data;
		
	}
	public static void main(String[] args) throws Exception{
		BayesJavaML classifier = new BayesJavaML();
		Dataset data = classifier.createDataset("FeaturizationNoMRDC/v1Matrix.txt", 44442);
		NormalizeMidrange nmr = new NormalizeMidrange(0, 8);
		nmr.build(data);
		nmr.filter(data);
//		FileHandler.exportDataset(data, new File("javamldata.txt"));
//		Dataset data = FileHandler.loadDataset(new File("javamldata.txt"));
		Sampling s = Sampling.SubSampling;
		Pair<Dataset, Dataset> datas = s.sample(data, (int)(data.size()*0.8));
		System.out.println("NAIVE BAYES");
		NaiveBayesClassifier naiveBayes = new NaiveBayesClassifier(true, true, true);
		naiveBayes.buildClassifier(datas.x());
		Map pms = EvaluateDataset.testDataset(naiveBayes, datas.y());
		System.out.println(pms);
		PerformanceMeasure perf = (PerformanceMeasure)pms.get("answered");
		System.out.println("F1: " + perf.getFMeasure());
		System.out.println("Accuracy: " + perf.getAccuracy());
		System.out.println("Recall: " + perf.getRecall());
		System.out.println("Precision: " + perf.getPrecision());
		System.out.println("features" + naiveBayes.getFeatureTable().size());
		
		Classifier nm = new NearestMeanClassifier();
		nm.buildClassifier(datas.x());
		Map nmPms = EvaluateDataset.testDataset(nm, datas.y());
		System.out.println(pms);
		PerformanceMeasure nmPerf = (PerformanceMeasure)nmPms.get("answered");
		System.out.println("F1: " + nmPerf.getFMeasure());
		System.out.println("Accuracy: " + nmPerf.getAccuracy());
		System.out.println("Recall: " + nmPerf.getRecall());
		System.out.println("Precision: " + nmPerf.getPrecision());
		
//		System.out.println("KNEARESTNEIGHBORS");
//		Classifier knn = new KNearestNeighbors(4);
//		knn.buildClassifier(datas.x());
//		Map knnPms = EvaluateDataset.testDataset(knn, datas.y());
//		System.out.println(knnPms);
//		PerformanceMeasure knnPerf = (PerformanceMeasure)knnPms.get("answered");
//		System.out.println("F1: " + knnPerf.getFMeasure());
//		System.out.println("Accuracy: " + knnPerf.getAccuracy());
//		System.out.println("Recall: " + knnPerf.getRecall());
//		System.out.println("Precision: " + knnPerf.getPrecision());

	}

}
