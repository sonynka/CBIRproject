package de.htw.cbir.feature;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;

import de.htw.cbir.model.Pic;
import de.htw.cbir.model.Settings;

public class ProjectMoments extends FeatureFactory
{
	
	int hQuant = 16;
	int sQuant = 4;
	int vQuant = 4;
	
	int dim = hQuant * sQuant * vQuant;
	int[] colorVector = new int[dim];
	
	public ProjectMoments(Settings settings) {
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

		float[] featureVector = new float[3 * 3 * 3];
		
		// loop over the block
		int r = 0; int g = 0; int b = 0;
		int numOfRegions = 3;
		int regHeight = height/numOfRegions;
		int n = width * regHeight;
		
		for(int reg = 0; reg < numOfRegions; reg++){
			float[] avgColor = new float[3];
			float[] deviation = new float[3];
			float[] skewness = new float[3];
			
			//calculate avgColor
			for(int y = reg * regHeight; y < reg * regHeight + regHeight; y++){
				for(int x = 0; x < width; x++){
					int pos = y * width + x;
					
					//get RGB values
					r =  (rgbValues[pos] >> 16) & 255;
					g =  (rgbValues[pos] >>  8) & 255;
					b =  (rgbValues[pos]      ) & 255;
					
					//convert RGB to HSV color space
					float[] hsv = new float[3];
					Color.RGBtoHSB(r,g,b,hsv);
					
					
					//https://en.wikipedia.org/wiki/Color_moments
					for(int i = 0; i < hsv.length; i++){
						avgColor[i] += hsv[i] / n;
					}
					
				}
			}
			
			//calculate variance and skewness
			for(int y = reg * regHeight; y < reg * regHeight + regHeight; y++){
				for(int x = 0; x < width; x++){
					int pos = y * width + x;
					
					//get RGB values
					r =  (rgbValues[pos] >> 16) & 255;
					g =  (rgbValues[pos] >>  8) & 255;
					b =  (rgbValues[pos]      ) & 255;
					
					//convert RGB to HSV color space
					float[] hsv = new float[3];
					Color.RGBtoHSB(r,g,b,hsv);
					
					//https://en.wikipedia.org/wiki/Color_moments
					for(int i = 0; i < hsv.length; i++){
						deviation[i] += Math.pow(hsv[i] - avgColor[i], 2);
						skewness[i] += Math.pow(hsv[i] - avgColor[i], 3);
					}
					
				}
			}
			
			for(int i = 0; i < avgColor.length; i++){
				deviation[i] = (float) Math.pow(deviation[i] / n, 0.5);
				if(skewness[i] >= 0) { skewness[i] = (float) Math.pow(skewness[i] / n, 0.3); }
				else skewness[i] = (float)(-1 * Math.pow(-skewness[i] / n, 0.3));
				
				featureVector[reg*9 + i*3] = avgColor[i];
				featureVector[reg*9 + i*3 + 1] = deviation[i];
				featureVector[reg*9 + i*3 + 2] = skewness[i];
			}
			
		}
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
		
		return getEuclidDistance(fv1, fv2);
	}

	@Override
	public String getName() {
		return "Color Moments";
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
	
	@Override
	public float[][] getFeatureVectors(Pic image){
		return null;
	}

	@Override
	public float getDistance(float[] i1fv1, float[] i2fv1, float[] i1fv2,
			float[] i2fv2) {
		return 0;
	}
	
}
