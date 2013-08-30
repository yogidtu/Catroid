package org.catrobat.catroid.speechrecognition.signalprocessing;

/**
 *  Catroid: An on-device visual programming system for Android devices
 *  Copyright (C) 2010-2013 The Catrobat Team
 *  (<http://developer.catrobat.org/credits>)
 *  
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *  
 *  An additional term exception under section 7 of the GNU Affero
 *  General Public License, version 3, is available at
 *  http://developer.catrobat.org/license_additional_term
 *  
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU Affero General Public License for more details.
 *  
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Filters an input power spectrum through a PLP filterbank. The filters in the filterbank are placed in the frequency
 * axis so as to mimic the critical band, representing different perceptual effect at different frequency bands. The
 * filter outputs are also scaled for equal loudness preemphasis. The filter shapes are defined by the {@link PLPFilter}
 * class. Like the {@link MelFrequencyFilterBank}, this filter bank has characteristics defined by the
 * {@link #PROP_NUMBER_FILTERS number of filters}, the {@link #PROP_MIN_FREQ minimum frequency}, and the
 * {@link #PROP_MAX_FREQ
 * maximum frequency}. Unlike the {@link MelFrequencyFilterBank}, the minimum and maximum frequencies here refer to the
 * <b>center</b> frequencies of the filters located at the leftmost and rightmost positions, and not to the edges.
 * Therefore, this filter bank spans a frequency range that goes beyond the limits suggested by the minimum and maximum
 * frequencies.
 * 
 * @author <a href="mailto:rsingh@cs.cmu.edu">rsingh</a>
 * @version 1.0
 * @see PLPFilter
 */
public class PLPFrequencyFilterBank {

	/** The property for the number of filters in the filterbank. */
	//@S4Integer(defaultValue = 32)
	public static final String PROP_NUMBER_FILTERS = "numberFilters";

	/** The property for the center frequency of the lowest filter in the filterbank. */
	//@S4Double(defaultValue = 130.0)
	public static final String PROP_MIN_FREQ = "minimumFrequency";

	/** The property for the center frequency of the highest filter in the filterbank. */
	//@S4Double(defaultValue = 3600.0)
	public static final String PROP_MAX_FREQ = "maximumFrequency";

	private int sampleRate;
	private int numberFftPoints;
	private int numberFilters;
	private double minFreq;
	private double maxFreq;
	private PLPFilter[] criticalBandFilter;
	private double[] equalLoudnessScaling;

	public PLPFrequencyFilterBank(double minFreq, double maxFreq, int numberFilters) {
		this.minFreq = minFreq;
		this.maxFreq = maxFreq;
		this.numberFilters = numberFilters;
	}

	public PLPFrequencyFilterBank() {
	}

	/**
	 * Build a PLP filterbank with the parameters given. The center frequencies of the PLP filters will be uniformly
	 * spaced between the minimum and maximum analysis frequencies on the Bark scale. on the Bark scale.
	 * 
	 * @throws IllegalArgumentException
	 */
	private void buildCriticalBandFilterbank() throws IllegalArgumentException {
		double minBarkFreq;
		double maxBarkFreq;
		double deltaBarkFreq;
		double nyquistFreq;
		double centerFreq;
		int numberDFTPoints = (numberFftPoints >> 1) + 1;
		double[] DFTFrequencies;

		/* This is the same class of warper called by PLPFilter.java */

		this.criticalBandFilter = new PLPFilter[numberFilters];

		if (numberFftPoints == 0) {
			throw new IllegalArgumentException("Number of FFT points is zero");
		}
		if (numberFilters < 1) {
			throw new IllegalArgumentException("Number of filters illegal: " + numberFilters);
		}

		DFTFrequencies = new double[numberDFTPoints];
		nyquistFreq = sampleRate / 2;
		for (int i = 0; i < numberDFTPoints; i++) {
			DFTFrequencies[i] = i * nyquistFreq / (numberDFTPoints - 1);
		}

		/**
		 * Find center frequencies of filters in the Bark scale
		 * translate to linear frequency and create PLP filters
		 * with these center frequencies.
		 * 
		 * Note that minFreq and maxFreq specify the CENTER FREQUENCIES
		 * of the lowest and highest PLP filters
		 */

		minBarkFreq = PLPFilter.hertzToBark(minFreq);
		maxBarkFreq = PLPFilter.hertzToBark(maxFreq);

		if (numberFilters < 1) {
			throw new IllegalArgumentException("Number of filters illegal: " + numberFilters);
		}
		deltaBarkFreq = (maxBarkFreq - minBarkFreq) / (numberFilters + 1);

		for (int i = 0; i < numberFilters; i++) {
			centerFreq = PLPFilter.barkToHertz(minBarkFreq + i * deltaBarkFreq);
			criticalBandFilter[i] = new PLPFilter(DFTFrequencies, centerFreq);
		}
	}

	/**
	 * This function return the equal loudness preemphasis factor at any frequency. The preemphasis function is given
	 * by
	 * <p/>
	 * E(w) = f^4 / (f^2 + 1.6e5) ^ 2 * (f^2 + 1.44e6) / (f^2 + 9.61e6)
	 * <p/>
	 * This is more modern one from HTK, for some reason it's preferred over old variant, and it doesn't require
	 * convertion to radiants
	 * <p/>
	 * E(w) = (w^2+56.8e6)*w^4/((w^2+6.3e6)^2(w^2+0.38e9)(w^6+9.58e26))
	 * <p/>
	 * where w is frequency in radians/second
	 * 
	 * @param freq
	 */
	private double loudnessScalingFunction(double freq) {
		double fsq = freq * freq;
		double fsub = fsq / (fsq + 1.6e5);
		return fsub * fsub * ((fsq + 1.44e6) / (fsq + 9.61e6));
	}

	/** Create an array of equal loudness preemphasis scaling terms for all the filters */
	private void buildEqualLoudnessScalingFactors() {
		double centerFreq;

		equalLoudnessScaling = new double[numberFilters];
		for (int i = 0; i < numberFilters; i++) {
			centerFreq = criticalBandFilter[i].centerFreqInHz;
			equalLoudnessScaling[i] = loudnessScalingFunction(centerFreq);
		}
	}

	/**
	 * Process data, creating the power spectrum from an input audio frame.
	 * 
	 * @param input
	 *            input power spectrum
	 * @return PLP power spectrum
	 * @throws java.lang.IllegalArgumentException
	 * 
	 */
	public double[] processFrame(double[] inputPowerSpectrum, int inputSampleRate) throws IllegalArgumentException {

		if (criticalBandFilter == null || sampleRate != inputSampleRate) {
			numberFftPoints = (inputPowerSpectrum.length - 1) << 1;
			sampleRate = inputSampleRate;
			buildCriticalBandFilterbank();
			buildEqualLoudnessScalingFactors();

		} else if (inputPowerSpectrum.length != ((numberFftPoints >> 1) + 1)) {
			throw new IllegalArgumentException("Window size is incorrect: in.length == " + inputPowerSpectrum.length
					+ ", numberFftPoints == " + ((numberFftPoints >> 1) + 1));
		}

		double[] outputPLPSpectralArray = new double[numberFilters];

		/**
		 * Filter input power spectrum
		 */
		for (int i = 0; i < numberFilters; i++) {
			// First compute critical band filter output
			outputPLPSpectralArray[i] = criticalBandFilter[i].filterOutput(inputPowerSpectrum);
			// Then scale it for equal loudness preemphasis
			outputPLPSpectralArray[i] *= equalLoudnessScaling[i];
		}

		return outputPLPSpectralArray;
	}

}