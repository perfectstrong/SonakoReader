package perfectstrong.sonako.sonakoreader.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import perfectstrong.sonako.sonakoreader.R;

/**
 * A singleton {@link Fragment} to display all downloading/ed entries
 */
public class PageDownloadFragment extends Fragment {

    private static PageDownloadFragment instance;
    private PageDownloadAdapter adapter = new PageDownloadAdapter();

    public static PageDownloadFragment getInstance() {
        if (instance == null)
            instance = new PageDownloadFragment();
        return instance;
    }

    public PageDownloadFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View rootView = inflater.inflate(R.layout.fragment_download, container, false);
        RecyclerView recyclerView = rootView.findViewById(R.id.DownloadJobRecyclerView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);

        // Adapter
        recyclerView.setAdapter(adapter);

        return rootView;
    }

    public void addJob(String title) {
        getInstance().adapter.addJob(new PageDownloadJob(title));
    }

    public void updateJob(String title, String progress) {
        getInstance().adapter.updateJob(title, progress);
    }

    public void removeJob(String title) {
        getInstance().adapter.removeJob(title);
    }

    private class PageDownloadJob {
        private final String title;
        private String progress;

        PageDownloadJob(String title) {
            this.title = title;
        }

        String getTitle() {
            return title;
        }

        String getProgress() {
            return progress;
        }

        void setProgress(String progress) {
            this.progress = progress;
        }
    }

    private class PageDownloadAdapter extends RecyclerView.Adapter<PageDownloadAdapter.PageDownloadViewHolder> {

        private List<PageDownloadJob> jobsList = new ArrayList<>();

        @NonNull
        @Override
        public PageDownloadViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.job_basic_view, viewGroup, false);
            return new PageDownloadViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull PageDownloadViewHolder pageDownloadViewHolder, int i) {
            pageDownloadViewHolder.initAt(i);
        }

        @Override
        public int getItemCount() {
            return jobsList.size();
        }

        void addJob(PageDownloadJob job) {
            jobsList.add(job);
            notifyDataSetChanged();
        }

        void updateJob(String title, String progress) {
            for (int i = 0; i < jobsList.size(); i++) {
                PageDownloadJob j = jobsList.get(i);
                if (j.getTitle().equals(title)) {
                    j.setProgress(progress);
                    notifyDataSetChanged();
                    return;
                }
            }
        }

        void removeJob(String title) {
            int idx = -1;
            for (int i = 0; i < jobsList.size(); i++) {
                PageDownloadJob j = jobsList.get(i);
                if (j.getTitle().equals(title)) {
                    idx = i;
                    break;
                }
            }
            if (idx < 0) return;
            jobsList.remove(idx);
            notifyDataSetChanged();
        }

        private class PageDownloadViewHolder extends RecyclerView.ViewHolder {

            View view;
            PageDownloadJob job;

            PageDownloadViewHolder(@NonNull View itemView) {
                super(itemView);
                view = itemView;
            }

            void initAt(int pos) {
                job = jobsList.get(pos);
                // Title
                ((TextView) view.findViewById(R.id.job_title)).setText(job.getTitle());
                // Progress
                ((TextView) view.findViewById(R.id.job_progress)).setText(job.getProgress());
            }
        }
    }
}
