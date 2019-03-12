package perfectstrong.sonako.sonakoreader.asyncTask;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import org.jsoup.Jsoup;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;
import perfectstrong.sonako.sonakoreader.helper.Utils;

public class AsyncMassLinkDownloader extends AsyncTask<Void, Void, List<String>> {

    private static final String TAG = AsyncMassLinkDownloader.class.getSimpleName();
    private final String title;
    private final String tag;
    private final WeakReference<Context> _context;
    private ProgressDialog progressDialog;

    public AsyncMassLinkDownloader(Context _context, String title, String tag) {
        this.title = title;
        this.tag = tag;
        this._context = new WeakReference<>(_context);
    }

    @Override
    protected void onPreExecute() {
        if (_context.get() != null)
            progressDialog = ProgressDialog.show(
                    _context.get(),
                    "",
                    _context.get().getString(R.string.loading_links)
            );
    }

    @Override
    protected List<String> doInBackground(Void... voids) {
        try {
            List<String> links = Jsoup.parse(
                    Utils.getTextFile(title, tag),
                    "UTF-8")
                    .select("a[data-ns='0']")
                    .eachAttr("title");
            Log.d(TAG, links.toString());
            return links;
        } catch (IOException e) {
            Log.e(TAG, "Error on parsing " + title, e);
            return null;
        }
    }

    @Override
    protected void onPostExecute(List<String> links) {
        Context context = _context.get();
        if (!isCancelled() && context != null) {
            if (progressDialog != null) progressDialog.dismiss();
            final List<String> selectedLinks = new ArrayList<>();
            if (links != null && !links.isEmpty())
                new AlertDialog.Builder(context)
                        .setTitle(R.string.which_link_to_download)
                        .setMultiChoiceItems(
                                links.toArray(new String[0]),
                                null,
                                (dialog, which, isChecked) -> {
                                    if (isChecked)
                                        selectedLinks.add(links.get(which));
                                    else
                                        selectedLinks.remove(links.get(which));
                                }
                        )
                        .setPositiveButton(
                                R.string.download_ok,
                                (dialog, which) -> Utils.massDownload(context, selectedLinks, tag, null)
                        )
                        .setNegativeButton(
                                R.string.download_no,
                                null
                        )
                        .setNeutralButton(
                                R.string.download_all,
                                (dialog, which) -> Utils.massDownload(
                                        context,
                                        links,
                                        tag,
                                        null)
                        )
                        .show();
            else
                new AlertDialog.Builder(context)
                        .setTitle(R.string.no_links_download)
                        .setMessage(R.string.try_redownload_text)
                        .setPositiveButton(
                                R.string.download_ok,
                                null
                        )
                        .show();
        }
    }
}
