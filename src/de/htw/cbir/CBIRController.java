package de.htw.cbir;

import java.util.Arrays;

import de.htw.cbir.evaluation.CBIREvaluation;
import de.htw.cbir.evaluation.PrecisionRecallTable;
import de.htw.cbir.feature.FeatureFactory;
import de.htw.cbir.feature.HistoAndMoments;
import de.htw.cbir.feature.ProjectHistogram;
import de.htw.cbir.feature.ProjectMoments;
import de.htw.cbir.model.Pic;
import de.htw.cbir.model.PicPair;
import de.htw.cbir.model.Settings;
import de.htw.cbir.model.Settings.SettingChangeEvent;
import de.htw.cbir.ui.CBIRView;

public class CBIRController {

	private Settings settings;
	private PicManager imageManager;
	
	private CBIRView ui;
	private FeatureFactory featureFactory;
	
	public CBIRController(Settings settings, PicManager imageManager) {
		this.settings = settings;
		this.imageManager = imageManager;
		
		// UI Elemente
		this.ui = new CBIRView(this);
		
		// Default Feature Factory
		changeFeatureFactory(getFeatureFactoryNames()[0]);
	}

	public PicManager getImageManager() {
		return imageManager;
	}
	
	public Settings getSettings() {
		return settings;
	}
	
	/**
	 * Berechne die Distanz aller Bilder zum Query Bild.
	 * Sortiere die Bilder von der kleinsten zur größten Distanz. 
	 * 
	 * @param queryImage
	 */
	public void sortByImage(Pic queryImage) {

		// wurde kein ein Sortieralgorithmus ausgewählt
		if(featureFactory == null) {
			System.out.println("No sorting algorithm selected");
			return;
		}
		
		Pic[] allImages = imageManager.getImages();
		long milliSec = System.currentTimeMillis();
		
		// Sortere das alle Bilder nach dem Querybild
		PicPair[] sortedArray = new PicPair[allImages.length];
		for (int i = 0; i < allImages.length; i++) {
			Pic searchImage = allImages[i];
			float distance = 0;
			
			if(featureFactory.getName().equalsIgnoreCase("HistogramMoments")){
				distance = featureFactory.getDistance(queryImage.getFeatureVectors()[0], searchImage.getFeatureVectors()[0], queryImage.getFeatureVectors()[1], searchImage.getFeatureVectors()[1]);
			
			} else {
				distance = featureFactory.getDistance(queryImage.getFeatureVector(), searchImage.getFeatureVector());
			}
			
			sortedArray[i] = new PicPair(queryImage, searchImage, distance);
		}
		
		// sortiere die Suchbilder nach der Distance zum Query Bild
		Arrays.sort(sortedArray);
		
		// berechne die mean average precision
		float ap = PrecisionRecallTable.calcAveragePrecision(sortedArray);

		// logge die Ergebnisse
		System.out.println("Feature Factory: "+featureFactory.getName());	
		System.out.printf("MAP: %2.4f took %6dms for feature factory %s\n\n", ap, (System.currentTimeMillis() - milliSec), featureFactory.getName());
		
		// wende die Reihnfolge an und zeige sie dem Benutzer
		for (int i = 0; i < sortedArray.length; i++) {
			sortedArray[i].getSearchImage().setRank(i);
		}
		ui.repaint();
	}

	/**
	 * Berechne die Mean Average Precision und zeichne dessen Graphen. 
	 * 
	 * @param category
	 */
	public void triggerTests(String category) {
		
		// wurde bereits ein Sortieralgorithmus ausgewählt
		if(featureFactory == null) {
			System.out.println("No sorting algorithm selected");
			return;
		}
		
		// evaluiere (durch MAP Wert) den Sortieralgorithmus
		Pic[] allImages = imageManager.getImages();
		CBIREvaluation eval = new CBIREvaluation(featureFactory, allImages);
		
		// welche Teste sollen durchgeführt werden
		Pic[] queryImages = (category.equals("All")) ? allImages : imageManager.getImageInCategory(category);
		long milliSec = System.currentTimeMillis();
		float map = eval.test(queryImages, true, category);
		System.out.printf("MAP: %2.4f took %6dms for feature factory %s and Category %s\n", map, (System.currentTimeMillis() - milliSec), featureFactory.getName(), category);
		
	}
	
	/**
	 * die Namen aller Feature Factories die im Menu auswählbar sein sollen
	 * 
	 * @return
	 */
	public String[] getFeatureFactoryNames() {
		return new String[] { "ProjectHistogram", "ProjectMoments", "HistogramMoments" };
	}
	
	/**
	 * Wähle eine andere Feature Factory aus und berechne alle Feature Vektoren mit dieser neu.
	 * 
	 * @param name
	 */
	public void changeFeatureFactory(String name) {
		settings.removeChangeListeners();
		
		if(name.equalsIgnoreCase("ProjectHistogram")) {
			featureFactory = new ProjectHistogram(settings);
		}
		
		else if(name.equalsIgnoreCase("ProjectMoments")) {
			featureFactory = new ProjectMoments(settings);
		}
		
		else if(name.equalsIgnoreCase("HistogramMoments")) {
			featureFactory = new HistoAndMoments(settings);
		}
		
		// erzeuge die Feature Vektoren
		if(featureFactory != null){
			calculateFeatureVectors(featureFactory, imageManager.getImages());
		}
			
	}
	
	protected void calculateFeatureVectors(FeatureFactory featureFactory, Pic[] images) {
		
		if(featureFactory.getName().equals("HistogramMoments")){
			for (Pic image : images) {
				image.setFeatureVectors(featureFactory.getFeatureVectors(image));
			}
		} else {
			for (Pic image : images) {
				image.setFeatureVector(featureFactory.getFeatureVector(image));
				image.setFeatureImage(featureFactory.getFeatureImage(image));
			}
		}
		ui.repaint();
	}	
}
