package de.tu.dresden.morseapp;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

//see http://www.tutorialspoint.com/android/android_camera.htm for the source

public class ReceiveActivity extends Activity {

	private Camera cameraObject;
	private ShowCamera showCamera;
	private ImageView pic;
	private CameraHandlerThread mThread = null;
	private Handler activityHandler;

	private static final String debugLabel = "MorseReceiverDebug";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityHandler = new Handler();
		setContentView(R.layout.activity_receive);
		pic = (ImageView) findViewById(R.id.imageView1);
		openCamera();
		cameraObject.setDisplayOrientation(90);
		showCamera = new ShowCamera(this, cameraObject);
		FrameLayout preview = (FrameLayout) findViewById(R.id.camera_preview);
		preview.addView(showCamera);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		cameraObject.release();
	}

	public void snapIt(View view) {
		cameraObject.takePicture(null, null, capturedIt);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public boolean hasSignal(Bitmap bitmap) {
		int whitecount = 0;
		int pixel;
		for (int x = 0; x < bitmap.getWidth(); x++) {
			for (int y = 0; y < bitmap.getHeight(); y++) {
				pixel = bitmap.getPixel(x, y);
				if ((Color.green(pixel) > 240) && (Color.red(pixel) > 240)
						&& (Color.blue(pixel) > 240)) {
					whitecount++;
				}
			}
		}

		if (whitecount > 300) {
			return true;
		}

		return false;

	}

	public Bitmap toGrayscale(Bitmap bmpOriginal) {
		int width, height;
		height = bmpOriginal.getHeight();
		width = bmpOriginal.getWidth();

		Bitmap bmpGrayscale = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		Canvas c = new Canvas(bmpGrayscale);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmpOriginal, 0, 0, paint);
		return bmpGrayscale;
	}

	private void openCamera() {
		if (mThread == null) {
			mThread = new CameraHandlerThread();
		}

		synchronized (mThread) {
			mThread.openCamera();
		}
	}

	private void handleSignal(final Bitmap bitmap, final boolean signal) {
		activityHandler.post(new Runnable() {
			@Override
			public void run() {
				pic.setImageBitmap(bitmap);
				if (signal) {
					Toast.makeText(getApplicationContext(), "Signal!",
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(getApplicationContext(), "No Signal.",
							Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

	private class CameraHandlerThread extends HandlerThread {
		Handler mHandler = null;

		CameraHandlerThread() {
			super("CameraHandlerThread");
			start();
			mHandler = new Handler(getLooper());
		}

		synchronized void notifyCameraOpened() {
			notify();
		}

		void openCamera() {
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					cameraObject = Camera.open();
					notifyCameraOpened();
				}
			});
			try {
				wait();
			} catch (InterruptedException e) {
				Log.d(debugLabel, "Wait for Camera was interrupted"); 
				//Waiting should be interrupted
			}
		}
	}

	private PictureCallback capturedIt = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
			bitmap = toGrayscale(bitmap);
			if (bitmap == null) {
				Log.d(debugLabel, "No Picture taken.");
			} else {
				handleSignal(bitmap,hasSignal(bitmap));
			}
			cameraObject.startPreview();
		}
	};
}
