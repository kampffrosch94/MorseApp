package de.tu.dresden.morseapp;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.NumberPicker;
import android.widget.TextView;

public class MainActivity extends Activity implements
		NumberPicker.OnValueChangeListener {

	private static final String debugLabel = "MorseApp MainActivity Debug";
	private EditText inputField;
	private CheckBox pausesBetweenChars;
	private List<MorseSendingWorker> workers;
	private List<String> messages;
	private ListView listViewForMessages;
	static Dialog dialog_freuquenz;
	public static int frequency;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		inputField = (EditText) findViewById(R.id.string_input_field);
		workers = new LinkedList<MorseSendingWorker>();
		messages = new LinkedList<String>();
		listViewForMessages = (ListView) findViewById(R.id.MessageList);
		pausesBetweenChars = (CheckBox) findViewById(R.id.pausesBetweenChars);
		frequency = 600;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			final Dialog dialog_freuquenz = new Dialog(MainActivity.this);
			dialog_freuquenz
					.setTitle("Insert new Frequenz in ms. Range: 60-1200");
			dialog_freuquenz.setContentView(R.layout.get_frequenz);
			Button b1 = (Button) dialog_freuquenz.findViewById(R.id.FrequenzOk);
			Button b2 = (Button) dialog_freuquenz
					.findViewById(R.id.FrequenzCancel);
			final NumberPicker np = (NumberPicker) dialog_freuquenz
					.findViewById(R.id.NumberPickerFrequenz);
			np.setMaxValue(1200);
			np.setMinValue(60);
			np.setValue(frequency);
			np.setWrapSelectorWheel(false);
			np.setOnValueChangedListener(this);
			b1.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					frequency = np.getValue();
					// addMessage(new Integer((int) freuquenz).toString());
					dialog_freuquenz.dismiss();
				}
			});
			b2.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					dialog_freuquenz.dismiss();

				}
			});
			dialog_freuquenz.show();

			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
		// TODO Auto-generated method stub

	}
	
	

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public void dispatchMorse(View view) {
		boolean pauses = pausesBetweenChars.isChecked();
		String stringToSend = inputField.getText().toString();

		addMessage("Send: " + stringToSend + '\n');
		Log.d(debugLabel, "input string was \"" + stringToSend + "\"");

		workers.add(new MorseSendingWorker(getApplicationContext(), frequency));
		workers.get(workers.size() - 1).execute("" + pauses, stringToSend);

	}

	public void cancelSending(View view) {
		for (MorseSendingWorker worker : workers) {
			worker.cancel(true);
		}
	}

	public void switchActivity(View view) {
		cancelSending(view);
		Intent intent = new Intent(getApplicationContext(),
				ReceiveActivity.class);
		startActivity(intent);
	}

	private void addMessage(String s) {
		messages.add(s);
		String[] listEmlements = messages.toArray(new String[messages.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_list_item_1, android.R.id.text1,
				listEmlements);
		listViewForMessages.setAdapter(adapter);

	}
}
