package de.htw.cbir.feature;

import java.awt.image.BufferedImage;

import de.htw.cbir.model.Pic;
import de.htw.cbir.model.Settings;


public class HistoAndMoments extends FeatureFactory
{
	
	public HistoAndMoments(Settings settings) {
		super(settings);
	}

	
	@Override
	public float getDistance(float[] fv1, float[] fv2) {	
		return 0;
	}
	
	@Override
	public float getDistance(float[] i1fv1, float[] i2fv1, float[] i1fv2, float[] i2fv2) {	
		
		float histoDistance = getL1Distance(i1fv1, i2fv1);
		float momentsDistance = getEuclidDistance(i1fv2, i2fv2);
		
		float weight = getWeightSetting();
		return weight * histoDistance + (1 - weight) * momentsDistance;
	}
	

	@Override
	public String getName() {
		return "HistogramMoments";
	}
	
	private float getWeightSetting() {
		return settings.getWeight();
	}
	

	@Override
	public BufferedImage getFeatureImage(Pic image) {
		return null;
	}

	@Override
	public float[] getFeatureVector(Pic image) {
		return null;
	}
	
	@Override
	public float[][] getFeatureVectors(Pic image){
		
		ProjectHistogram histogramFeature = new ProjectHistogram(settings);
		ProjectMoments momentsFeature = new ProjectMoments(settings);
		
		float[][] featureVectors = new float[2][];
		featureVectors[0] = histogramFeature.getFeatureVector(image);
		featureVectors[1] = momentsFeature.getFeatureVector(image);
		
		return featureVectors;
	}
	
}
