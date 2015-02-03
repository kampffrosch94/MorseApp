package de.tu.dresden.morseapp;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.ImageFormat;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
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
	LinkedList<Long> signalTimeList = null;
	FlashDecoder2 flashdecoder;
	MorseTranslator morsetranslator;

	private static final String debugLabel = "MorseReceiverDebug";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		activityHandler = new Handler();
		setContentView(R.layout.activity_receive);

		morsetranslator = MorseTranslator.getInstance();
		flashdecoder = new FlashDecoder2();

		// Test
		LinkedList<Long> testlist = new LinkedList<Long>();
		testlist.add((long) 500); // dit
		testlist.add((long) 500);
		testlist.add((long) 500); // dit
		testlist.add((long) 3500); // dat
		testlist.add((long) 1500); // pause
		testlist.add((long) 500); // dit
		String result = flashdecoder.decode(testlist);
		Log.i("MorseCode", result);
		Log.i("MorseCode", "End of test.");

		// End Test

		pic = (ImageView) findViewById(R.id.imageView1);
		openCamera();

		Camera.Parameters para = cameraObject.getParameters();
		para.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_CLOUDY_DAYLIGHT);

		List<Size> sizes = para.getSupportedPreviewSizes();
		for (Size size : sizes) {
			Log.d("CameraSettings", "Available resolution: " + size.width + " "
					+ size.height);
		}
		cameraObject.setParameters(para);

		List<Integer> rates = para.getSupportedPreviewFrameRates();

		for (Integer rate : rates) {
			Log.d("CameraSettings", "Available framerate: " + rate.toString());
		}

		Size size = sizes.get(sizes.size() - 1);
		para.setPreviewSize(size.width, size.height);
		Log.d("CameraSettings", "Set resolution: " + size.width + " "
				+ size.height);

		if (sizes.contains(10)) {
			para.setPreviewFrameRate(10);
			Log.d("CameraSettings", "Set framerate: 10 per second");
		}

		cameraObject.setPreviewCallback(previewCb);

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
		cameraObject.stopPreview();
		mThread.suspend();
		cameraObject.release();
	}

	public void snapIt(View view) {
		cameraObject.takePicture(null, null, capturedIt);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
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
				// Waiting should be interrupted
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
				handleSignal(bitmap, hasSignal(bitmap));
			}
			cameraObject.startPreview();
		}
	};

	public void handleList(long start, long end) {
		long delta = end - start;
		Log.d("Signaltime", "Length : " + Long.toString(delta));
		if (signalTimeList == null) {
			signalTimeList = new LinkedList<Long>();
		}
		signalTimeList.add(delta);

		String morsecode = flashdecoder.decode(signalTimeList);
		Log.i("MorseCode", morsecode);
		Log.i("MorseResult", morsetranslator.morseToString(morsecode));
	}

	private boolean started = false;
	private boolean signalLast = false; // state of the previous signal
	private long signalChangeTime;

	public void handleTick(boolean signal) {
		if (!started && signal) {
			started = true;
			signalLast = true;
			signalChangeTime = System.currentTimeMillis();
		}

		if ((signalLast != signal) && started) {
			signalLast = signal;
			long currentTime = System.currentTimeMillis();
			handleList(signalChangeTime, currentTime);
			signalChangeTime = currentTime;
		}

	}

	private PreviewCallback previewCb = new PreviewCallback() {

		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {

			// Convert to JPG
			Size previewSize = camera.getParameters().getPreviewSize();
			YuvImage yuvimage = new YuvImage(data, ImageFormat.NV21,
					previewSize.width, previewSize.height, null);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			yuvimage.compressToJpeg(new Rect(0, 0, previewSize.width,
					previewSize.height), 80, baos);
			byte[] jdata = baos.toByteArray();

			// Convert to Bitmap
			Bitmap bitmap = BitmapFactory.decodeByteArray(jdata, 0,
					jdata.length);

			if (bitmap == null) {
				Log.d(debugLabel, "No Picture decoded.");
			} else {
				bitmap = toGrayscale(bitmap);
				handleTick(hasSignal(bitmap));
				Log.d(debugLabel, "Picture decoded.");
			}
		}

	};
}
