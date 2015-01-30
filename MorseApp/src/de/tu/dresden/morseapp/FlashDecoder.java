package de.tu.dresden.morseapp;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class FlashDecoder
{
	private static final String debugLabel = "FlashDecoder Debug";
	private static long dit;
	private static final float tolerance = 0.1F;
	
	/*
	 * This is the main method which should be called to decode the times.
	 * First Element of the parameter "times" must be the first time the flash is enabled. Also currently the string must contain at least 2 words
	 * and 2 chars per word, and also at least one dash per char.
	 * 
	 * TODO add a calibration mode, so that the restrictions above are no longer needed (calibration is standard for morse)
	 */
	public static LinkedList<String> decodeFlash(List<Long> times)
	{
		LinkedList<String> result = new LinkedList<String>();
		LinkedList<List<Long>> wordTimes = splitTimesIntoWords(times);
		for(List<Long> words : wordTimes)
		{
			for(List<Long> charTimes :splitTimesIntoChars(words))
			{
				result.add(decodeSingleChar(charTimes));
			}
			result.add("/");
		}
		result.removeLast();
		return result;
	}
	
	/*
	 * This splits the whole times list into mutliple times lists, one for each whole word.
	 */
	private static LinkedList<List<Long>> splitTimesIntoWords(List<Long> times)
	{
		LinkedList<List<Long>> result = new LinkedList<List<Long>>();
		long longestPause = 0;
		for(int i = 1; i < times.size() / 2; i++)
		{
			long pause = (times.get(2 * i) - times.get (2 * i-1));
			longestPause = (longestPause < pause) ? pause : longestPause ;
		} 
		
		for(int i = 0; i < times.size(); i++)
		{
			int last = 0;
			long interval = times.get(i + 1) - times.get(i);
			if( interval > longestPause * (1-tolerance) &&  interval < longestPause * (1+tolerance) )
			{
				result.add(times.subList(last, i));
				last = i + 1;
			}
		}
		
		return result;
	}
	
	
	private static LinkedList<List<Long>> splitTimesIntoChars(List<Long> words)
	{
		return splitTimesIntoWords(words);
	}

	/*
	 * This takes a list of recorded flash enable/disables times and returns a string of detected
	 * morse symbols. 
	 * The Argument must be the times for one char only. This means there may be only 1 dit long pauses.
	 */
	private static String decodeSingleChar(List<Long> times)
	{
		String result = "";
		int totalPauses = 0;
		long pauseSum = 0;
		long dit = 0;
		for(int i = 1; i < times.size() / 2; i++)
		{
			Log.d(debugLabel, i + ". Pause:" + (times.get(2 * i) - times.get (2 * i-1)));
			totalPauses++;
			pauseSum += (times.get(2 * i) - times.get (2 * i-1));
		}
		
		dit = pauseSum / totalPauses;
		for(int i = 0; i < times.size(); i++)
		{
			long interval = times.get(i + 1) - times.get(i);
			if( interval > dit * (1-tolerance) &&  interval < dit * (1+tolerance) )
			{
				result += '.';
				continue;
			}
			if( interval > 3*dit * (1-tolerance) &&  interval < 3*dit * (1+tolerance) )
			{
				result += '-';
				continue;
			}
		}
		return result;
	}
	
	/*
	 * This calibrates the FlashDecoder to read morse.
	 * By convention, the first time send through the stream must mark a "flash on",
	 * To calibrate morse, first a "KA" must be send, following by "Paris".
	 * Calibration must be started while KA is being send and cover at least one point and one dash. Between "KA" and "Paris" must be the regular morse break between words (7*dit)
	 */
	public static void calibrate(InputStream inStream)
	{
		long temporaryDit = 0;
		long timeLastOff = 0;
		long timeOn = 0;
		long timeOff = 0;
		boolean KAsend = false;
		boolean rebasedDitTime = false;

		reading:
		while(true)
		{
			if(timeOn != 0 && timeOff != 0)
			{
				timeOn = 0;
				timeOff = 0;
			}
			try
			{
				if(timeOn == 0)
				{
					timeOn = (long) inStream.read();
					continue reading;
				}
				else
				{
					timeLastOff = timeOff;
					timeOff = (long) inStream.read();
				}
			}
			catch (IOException e)
			{
				break reading;
			}
			
			//this will be true as long as not the entire KA including the break after KA is send
			if(KAsend == false)
			{
				long newDit = timeOff - timeOn;
			
				if(rebasedDitTime)
				{
					if(timeLastOff - timeOn > (7*temporaryDit) * (1-tolerance) && timeLastOff - timeOn < (7*temporaryDit) * (1+tolerance))
						KAsend = true;
					continue reading;
				} 
				if(temporaryDit == 0)
				{
					temporaryDit = newDit;
					continue reading;
				}
				if(temporaryDit * (1+tolerance) > newDit && temporaryDit * (1-tolerance) > newDit)
				{
					temporaryDit = newDit;
				}
				//we will reach this the second time
				rebasedDitTime = true;

			}
			else
			{
				long accumulatedDitTimes = 0;
				int totalDitsRead = 0;
				//start reading "Paris". because this is always the same, we dont need to read and sanity check. however if paris is not send at this time, calibration will fail
				//'P' -..- 2 + 3 = 5 dits
				//'A' .- 1 + 2 = 3 dits
				//'R' .-. 2 + 2 = 4 dits
				//'I' .. 2 dits
				//'S' ... 3 dits
				int ditsPerParis = (5+3+4+2+3);
				while(totalDitsRead < ditsPerParis)
				{
					accumulatedDitTimes += timeOn - timeOff;
				}
				Log.d(debugLabel, "berechnete dit länge: " + dit);
				dit = accumulatedDitTimes / ditsPerParis;
				
				
			}
				
				
		}		
		
		
	}
	
}
