package de.tu.dresden.morseapp;

import java.util.LinkedList;
import java.util.List;

import android.util.Log;

public class FlashDecoder
{
	private static final String debugLabel = "FlashDecoder Debug";
	private static final float tolerance = 0.1F;
	
	/*
	 * This is the main method which should be called to decode the times.
	 * First Element of the parameter "times" must be the first time the flash is enabled. Also currently the string must contain at least 2 words
	 * and 2 chars per word.
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
	
}
