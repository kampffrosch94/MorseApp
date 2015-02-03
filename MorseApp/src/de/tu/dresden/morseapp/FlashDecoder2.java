package de.tu.dresden.morseapp;

import java.util.List;

import android.util.Log;

// Same as FlashDecoder, but realizing the KISS-Principle
public class FlashDecoder2 {

	static final double tolerance = 2.5; // how many times bigger than the
										// smallest is still counted as dit

	long ditlength;

	public FlashDecoder2() {
		// TODO Auto-generated constructor stub
	}

	public long calibrate(List<Long> signals) {
		long smallest = signals.get(0);
		for (Long signal : signals) {
			if (smallest < signal) {
				smallest = signal;
			}
		}

		return smallest;
	}

	public String decode(List<Long> signals) {
		String result = "";
		Log.i("MorseCode", "Signalcount: " + signals.size());
		ditlength = calibrate(signals);
		boolean light = true; // is light on or off
		long signal;
		for (int i = 0; i < signals.size(); i++) {
			signal = signals.get(i);
			if (light) {
				if (isDit(signal)) {
					result += ".";
					Log.i("MorseCode", "added dit");
				} else if(isDat(signal)){
					result += "-";
					Log.i("MorseCode", "added dat");
				}
				light = false;
			} else { //light is off
				if(isPause(signal))
					result += "/";
				light = true;
			}
			
		}
		return result;
	}
	

	public boolean isDit(long signal) {
		if (signal >= ditlength && signal <= tolerance * ditlength)
			return true;
		else
			return false;
	}

	public boolean isDat(long signal) {
		if (signal > tolerance * ditlength)
			return true;
		else
			return false;
	}

	public boolean isPause(long signal) { //pause between words
		if (signal > 2 * tolerance * ditlength)
			return true;
		else
			return false;
	}
}
