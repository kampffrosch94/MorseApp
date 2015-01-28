package de.tu.dresden.morseapp;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Semaphore;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.util.Log;


/**
 * @author redbreastbird
 *
 */
public class MorseSendingWorker extends AsyncTask<String, Object, Boolean>
{
	private MorseTranslator translator = MorseTranslator.getInstance();
	private Context context;
	private Camera cam;
	private long currentTime;
	private static Semaphore sema  = new Semaphore(1);
	
	public static int dit = 600;
	public static int dah = 3 * dit;
	public static int pause = dit;
	public static int interword_pause = 7*dit;
	
	
	
	public MorseSendingWorker(Context context)
	{
		this.context = context;
	}
	
	@Override
	protected Boolean doInBackground(String... words)
	{
		if(cam == null)
			return false;
		
		for(String word : words)
		{
			sendByFlash(translator.stringToMorse(word));
		}
		
		return true;
	}

	private void sendByFlash(List<String> morse)	
	{
		for(String code : morse)
		{
			for(char c : code.toCharArray())
			{
				if(c == '.')
				{
					//DEBUG
					Log.d("DEBUG", "punkt");
					//DEBUG
					while(System.currentTimeMillis() - currentTime < pause)
						;
					flashOn();
					elapseTime(dit);
				    flashOff();
				    currentTime = System.currentTimeMillis();
					continue;
				}
				if(c == '-')
				{
					//DEBUG
					Log.d("DEBUG", "dash");
					//DEBUG
					while(System.currentTimeMillis() - currentTime < pause)
						;
					flashOn();
					elapseTime(dah);
					flashOff();
					currentTime = System.currentTimeMillis();
					continue;
				}
				if(c == '/')
				{
					//DEBUG
					Log.d("DEBUG", "end of word");
					//DEBUG
					elapseTime(interword_pause);
					continue;
				}
				
			}
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
		
		try
		{
			sema.acquire();
		}
		catch (InterruptedException e)
		{
			//continue exectuion;
		}
		
		try
		{
			cam = Camera.open();
			cam.setPreviewTexture(new SurfaceTexture(0));
		}
		catch(Exception ex)
		{
			//TODO handle camera not available; execution of onPreExcecute should stop here
			Log.d("ERROR", "Camera not available");
			ex.printStackTrace();
		}
		
		assert cam != null; 
		
		Camera.Parameters p = cam.getParameters();
		p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
		cam.setParameters(p);
		cam.startPreview();
		currentTime = System.currentTimeMillis();
		
	}
	
	@Override
	protected void onPostExecute(Boolean result)
	{
		super.onPostExecute(result);
		
		if(result)
		{	
			//cam.release();
		}
		try
		{
			//we do this to avoid sending another message too soon
			Thread.sleep(interword_pause);
		}
		catch(InterruptedException ex)
		{	
		}
		cam.stopPreview();
		cam.release();
		sema.release();
		
	}
	
	private void flashOn()
	{
		try
		{
			//Camera.Parameters p = cam.getParameters();
			//p.setFlashMode(Camera.Parameters.FLASH_MODE_ON);
			//cam.setParameters(p);
			//cam.setPreviewTexture(new SurfaceTexture(0));
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
			//Camera.Parameters p = cam.getParameters();
			//p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
			//cam.setParameters(p);
			cam.cancelAutoFocus();		
		}
		catch(Exception ex)
		{
			ex.printStackTrace();
		}
	}
	
	private void elapseTime(int ms)
	{
		//this is busy waiting, i know, but using interrupts is less precise
		long startTime = System.currentTimeMillis();
		long elapsed = 0;
		while(elapsed < ms)
			elapsed = System.currentTimeMillis() - startTime;
	}
}
