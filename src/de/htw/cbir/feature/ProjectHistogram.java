package de.htw.cbir.feature;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import de.htw.cbir.model.Pic;
import de.htw.cbir.model.Settings;

public class ProjectHistogram extends FeatureFactory
{
	
	int hQuant = 16;
	int sQuant = 4;
	int vQuant = 4;
	
	int dim = hQuant * sQuant * vQuant;
	int[] colorVector = new int[dim];
	
	public ProjectHistogram(Settings settings) {
		super(settings);
	}

	///////////////////////////////////////////
	// visualize the feature data as image
	//
	@Override
	public BufferedImage getFeatureImage(Pic image) {

		int width = dim;
		int height = 100;

		BufferedImage bi = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D big = bi.createGraphics();
		
				
		float[] featureVector = image.getFeatureVector();
		
		for(int i = 0; i < featureVector.length; i++){
			big.setColor(new Color(colorVector[i]));
			big.fillRect(i, (int)(height - (featureVector[i] * height)), 1, (int)(featureVector[i] * height));
		}

		big.dispose();
		return bi;
	}

	@Override
	public float[] getFeatureVector(Pic image)  
	{
		
		//TODO: convert RGB to HSB - Color.RGBtoHSB(r,g,b,hsv)
		BufferedImage bi = image.getDisplayImage();

		int width  = bi.getWidth();
		int height = bi.getHeight();

		int [] rgbValues = new int[width * height];
		bi.getRGB(0, 0, width, height, rgbValues, 0, width);

		float[] featureVector = new float[dim];
		
		// loop over the block
		int r = 0; int g = 0; int b = 0;
		
		for(int y = 0; y < height; y++) {
			for (int x = 0 ; x < width ; x++) {
				int pos = y * width + x;
				
				//get RGB values
				r =  (rgbValues[pos] >> 16) & 255;
				g =  (rgbValues[pos] >>  8) & 255;
				b =  (rgbValues[pos]      ) & 255;
				
				//convert RGB to HSV color space
				float[] hsv = new float[3];
				Color.RGBtoHSB(r,g,b,hsv);
				
				//quantify color space
				hsv[0] = quantifyValue(hsv[0], hQuant);  //h
				hsv[1] = quantifyValue(hsv[1], sQuant);  //s
				hsv[2] = quantifyValue(hsv[2], vQuant);  //v
				
				//select histogram bin
				int hBin = Math.round(hsv[0] * (hQuant - 1));
				int sBin = Math.round(hsv[1] * (sQuant - 1));
				int vBin = Math.round(hsv[2] * (vQuant - 1));
				
				//flatten 3D to 1D array
				//featureVector[hBin][sBin][vBin] = hsv;
				featureVector[vBin + vQuant * (sBin + sQuant * hBin)]++;
				
				//save the colors for representation of histogram
				colorVector[vBin + vQuant * (sBin + sQuant * hBin)] = Color.HSBtoRGB(hsv[0], hsv[1], hsv[2]);
				
			}
		}
		
		normalizeHistogram(featureVector);
//		normalizeHistogram2(featureVector, width*height);
		return featureVector;
	}

	private double getSaturationSetting() {
		return settings.getSaturation() / 10;
	}
	
	public void normalizeHistogram(float[] histogram){
		//logaritmische daempfung
		for(int i = 0; i < histogram.length; i++){
			histogram[i] = (float) Math.log(histogram[i] + 1);
		}
		
		//find maximum
		double max = 0;
		for (int i = 0; i < histogram.length; i++) {
			
			if (histogram[i] > max)
				max = histogram[i];
		}
		
		//normalize
		for(int i = 0; i < histogram.length; i++){
			histogram[i] /= max;
		}
		
	}
	

	public void normalizeHistogram2(float[] histogram, int numOfPixels){
		
		//normalize
		for(int i = 0; i < histogram.length; i++){
			histogram[i] /= numOfPixels;
		}
		
	}
	
	@Override
	public float getDistance(float[] fv1, float[] fv2) {	
		
		return getL1Distance(fv1, fv2);
	}

	@Override
	public String getName() {
		return "Histogramm";
	}
	
	
	public float quantifyValue(float input, int factor) {

	        if (input > 1.0f || input < 0 || factor < 2) {
	            throw new RuntimeException("invalid input " + input + " " + factor);
	        }

	        float step = 1.0f / (factor - 1);
	        float halfStep = step / 2f;

	        float value = 0f;

	        do {
	            if (input >= value - halfStep && input < value + halfStep) {
	                return value;
	            }

	            value += step;
	        } while (value <= 1f);
	        
	        return 1f;
	}
	
	public float[][] getFeatureVectors(Pic image){
		return null;
	}

	@Override
	public float getDistance(float[] i1fv1, float[] i2fv1, float[] i1fv2,
			float[] i2fv2) {
		return 0;
	}
	
}
