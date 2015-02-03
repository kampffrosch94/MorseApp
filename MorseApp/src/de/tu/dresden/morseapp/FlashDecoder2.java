package de.tu.dresden.morseapp;

import java.util.List;

// Same as FlashDecoder, but realizing the KISS-Principle
public class FlashDecoder2 {

	static final double tolerance = 2; // how many times bigger than the
										// smallest is still counted as dit

	int ditlength;

	public FlashDecoder2() {
		// TODO Auto-generated constructor stub
	}

	public int calibrate(List<Integer> signals) {
		int smallest = signals.get(0);
		for (Integer signal : signals) {
			if (smallest < signal) {
				smallest = signal;
			}
		}

		return smallest;
	}

	public String decode(List<Integer> signals) {
		String result = "";
		ditlength = calibrate(signals);
		boolean light = true; // is light on or off
		for (Integer signal : signals) {
			if (light) {
				if (isDit(signal)) {
					result += '.';
				} else if(isDat(signal)){
					result += '-';
				}

			} else { //light is off
				if(isPause(signal))
					result += '/';
			}
			light = !light;
		}
		return result;

	}

	public boolean isDit(int signal) {
		if (signal >= ditlength && signal <= tolerance * ditlength)
			return true;
		else
			return false;
	}

	public boolean isDat(int signal) {
		if (signal > tolerance * ditlength)
			return true;
		else
			return false;
	}

	public boolean isPause(int signal) { //pause between words
		if (signal > 2 * tolerance * ditlength)
			return true;
		else
			return false;
	}
}
