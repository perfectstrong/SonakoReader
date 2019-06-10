package perfectstrong.sonako.sonakoreader.asyncTask;

import android.os.AsyncTask;
import android.widget.Toast;

import perfectstrong.sonako.sonakoreader.SonakoReaderApp;

public abstract class StaticMessageUpdateAsyncTask<Input, Output> extends AsyncTask<Input, Integer, Output> {
    @Override
    protected void onProgressUpdate(Integer... values) {
        Toast.makeText(
                SonakoReaderApp.getContext(),
                values[0],
                Toast.LENGTH_SHORT
        ).show();
    }
}
