package de.tu.dresden.morseapp;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.AsyncTask;
import android.util.Log;


/**
 * @author redbreastbird
 * 
 * This was tested on:
 * 		Nexus 5 with 5.0.1
 * 		S4 mini with 4.2.2 
 * 		S2 		with 4.4.4
 *		
 * TODO test on other devices and add check if this doesnt work there
 *
 */
public class MorseSendingWorker extends AsyncTask<String, Object, Boolean>
{
	private MorseTranslator translator = MorseTranslator.getInstance();
	private Context context;
	private Camera cam;
	private long currentTime;
	private static Semaphore sema  = new Semaphore(1);
	private boolean hasSema = false;
	
	private static final String debugLabel = "MorseSendingDebug";
	private static final String MorseSendingError = "MorseSendingDebug";
	
	private int dit; //length of one point, defines all other values
	private int dah; //length of one dash, length of pause between last symbol of a char and first symbol of next char
	private int pause; //length of pause between 2 symbols of one char
	private int interword_pause; //length of pause between 2 whole words
	
	
	
	public MorseSendingWorker(Context context, int dit)
	{
		this.dit = dit;
		this.dah = 3 * dit;
		this.pause = dit;
		this.interword_pause = 7*dit; 
		this.context = context;
	}
	
	@Override
	protected Boolean doInBackground(String... words)
	{
		if(words.length < 2)
			throw new RuntimeException("First word must be a boolean determining pauses between chars or not, second one must be the word to send");
		
		try
		{
			sema.acquire();
			hasSema = true;
		}
		catch (InterruptedException e)
		{
			/* continue execution
			 * other string was already being send
			 */
		}
		
		try
		{
			cam = Camera.open();
			cam.setPreviewTexture(new SurfaceTexture(0));
		}
		catch(Exception ex)
		{
			Log.d(MorseSendingError, "Camera not available");
			ex.printStackTrace();
		}
		
		Camera.Parameters p = cam.getParameters();
		p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		cam.setParameters(p);
		cam.startPreview();
		
		if(cam == null)
			return false;
		
		//send all strings except for the first, which must be a boolean determining pauses between chars or not
		String[] words2 = Arrays.copyOfRange(words, 1, words.length);
		for(String word : words2)
		{
			sendByFlash(translator.stringToMorse(word), Boolean.parseBoolean(words[0]));
		}		
		return true;
	}

	private void sendByFlash(List<String> morse, boolean withBreaksbetweenChars)	
	{
		for(String code : morse)
		{
			Log.d(debugLabel, code);

			for(char c : code.toCharArray())
			{
				if(isCancelled())
					return;
				if(c == '.')
				{
					Log.d(debugLabel, "point");
					if((pause - (System.currentTimeMillis() - currentTime)) > 0 )
						elapseTime((pause - (System.currentTimeMillis() - currentTime)));
					flashOn();
					elapseTime(dit);
				    flashOff();
				    currentTime = System.currentTimeMillis();
					continue;
				}
				if(c == '-')
				{
					Log.d(debugLabel, "dash");
					if((pause - (System.currentTimeMillis() - currentTime)) > 0 )
						elapseTime((pause - (System.currentTimeMillis() - currentTime)));
					flashOn();
					elapseTime(dah);
					flashOff();
					currentTime = System.currentTimeMillis();
					continue;
				}
				if(c == '/')
				{
					Log.d(debugLabel, "end of word");
					//we need this - dah, because we hard-wait 1 dah after every char
					elapseTime(interword_pause - dah);
					continue;
				}
				
			}
			if(!withBreaksbetweenChars) 
				elapseTime(dah);
		}
	}

	@Override
	protected void onPreExecute()
	{
		super.onPreExecute();
		
		if(!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH))
		{
			throw new RuntimeException(context.getString(R.string.flash_feature_not_available));
		}
		
		currentTime = System.currentTimeMillis();
		
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		
		cam.stopPreview();
		cam.release();
		sema.release();
		
	}
	
	@Override
	protected void onCancelled()
	{
		super.onCancelled();
		if(cam != null)
		{
			cam.stopPreview();
			cam.release();
			if(hasSema)
				sema.release();
		}
			
		
	}
	
	private void flashOn()
	{
		try
		{
			Camera.Parameters p = cam.getParameters();
			p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			cam.autoFocus(null);
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	private void flashOff()
	{
		try
		{
			Camera.Parameters p = cam.getParameters();
			p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			cam.setParameters(p);
			cam.cancelAutoFocus();		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void elapseTime(long ms)
	{
		try
		{
			Thread.sleep(ms, 1000);
		} catch (InterruptedException e)
		{
			Log.d(debugLabel, "Cancel was called");
		}
	}
}
