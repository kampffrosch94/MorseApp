package de.tu.dresden.morseapp;

import java.util.LinkedList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ListView;


public class MainActivity extends Activity
{

	private static final String debugLabel = "MorseApp MainActivity Debug";
	private EditText inputField;
	private CheckBox pausesBetweenChars;
	private List<MorseSendingWorker> workers;
	private List<String> messages;
	private ListView listViewForMessages;
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        inputField = (EditText) findViewById(R.id.string_input_field);
        workers = new LinkedList<MorseSendingWorker>();
        messages = new LinkedList<String>();
        listViewForMessages = (ListView) findViewById(R.id.MessageList);
        pausesBetweenChars = (CheckBox) findViewById(R.id.pausesBetweenChars);
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume()
    {
    	// TODO Auto-generated method stub
    	super.onResume();
    }
    
    @Override
    protected void onPause()
    {
    	// TODO Auto-generated method stub
    	super.onPause();
    }
    
    public void dispatchMorse(View view)
    {
    	boolean pauses = pausesBetweenChars.isChecked();
    	String stringToSend = inputField.getText().toString();
    	
    	addMessage("Send: "+ stringToSend + '\n');
    	Log.d(debugLabel, "input string was \"" + stringToSend + "\"");
    	
    	workers.add(new MorseSendingWorker(getApplicationContext(), 600));
    	workers.get(workers.size() - 1).execute(""+ pauses, stringToSend);
    	
    }
    
    public void cancelSending(View view)
    {
    	for(MorseSendingWorker worker : workers)
    	{
    		worker.cancel(true);
    	}
    }
    
	public void switchActivity(View view) {
		cancelSending(view);
		Intent intent = new Intent(getApplicationContext(), ReceiveActivity.class);
		startActivity(intent);
	}
	
	private void addMessage(String s){
		messages.add(s);
		String[] listEmlements = messages.toArray(new String[messages.size()]);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, listEmlements);
		listViewForMessages.setAdapter(adapter);
	
	}
}
