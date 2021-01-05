package com.example.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import androidx.appcompat.widget.SearchView;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link photoGalleryFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class photoGalleryFragment extends Fragment {
    private RecyclerView mRecyclerView;
    private List<GalleryItem> mItems = new ArrayList<>();
    private ThumnailDownloader<PhotoHolder> mthumnailDownloader;
    private static final String TAG = "PhotoGalleryFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public photoGalleryFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment photoGalleryFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static photoGalleryFragment newInstance() {
        photoGalleryFragment fragment = new photoGalleryFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);
        //args.putString(ARG_PARAM2, param2);
        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
       // new FetchItemTask().execute();
        updateItem();
        setRetainInstance(true);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        Handler responseHandler = new Handler();
        mthumnailDownloader = new ThumnailDownloader<>(responseHandler);
        mthumnailDownloader.setThumbnailDownloadListener(
                new ThumnailDownloader.ThumbnailDownloadListener<PhotoHolder>() {

                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail) {
                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindGallery(drawable);
                    }
                }
        );
        mthumnailDownloader.start();
        mthumnailDownloader.getLooper();
        Log.i(TAG, "Background thread started");


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mthumnailDownloader.quit();
        Log.i(TAG, "Background thread destroyed");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_photo_gallery,menu);

        //Update Recycler view on Search
        MenuItem search =menu.findItem(R.id.search);
        SearchView searchView = (SearchView) search.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                QueryPreferences.storeQuery(getActivity(),s);
                updateItem();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });
    }

    private void updateItem() {
        String query = QueryPreferences.getStoredQuery(getActivity());
        new FetchItemTask(query).execute();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clear:
                QueryPreferences.storeQuery(getActivity(),null);
                updateItem();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mthumnailDownloader.clearQueue();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = view.findViewById(R.id.phtoGalleryFrag_rv);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(),3));
        setupAdapter ();
        return view;
    }

    private void setupAdapter() {
        if (isAdded()){
            mRecyclerView.setAdapter(new PhotoAdapter(mItems));
        }
    }

    private class FetchItemTask extends AsyncTask<Void,Void,List<GalleryItem>>{

        private String query;
        public FetchItemTask(String query) {
            this.query = query;
        }



        @Override
        protected List<GalleryItem> doInBackground(Void... params) {

            //String query = "robots";

            if (query==null){
            return new FlickFetchr().fetchRecent_photos();
            }else {
                return new FlickFetchr().SearchPhoto(query);
            }


        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mItems = galleryItems;
            setupAdapter();
        }
    }

    private class PhotoHolder extends RecyclerView.ViewHolder{
        //private TextView txt
        private ImageView img ;
        public PhotoHolder(@NonNull View itemView) {
            super(itemView);
            img = itemView.findViewById(R.id.galleryImg);

        }

        public void bindGallery(Drawable drawable){

           // txt.setText(item.toString());
            img.setImageDrawable(drawable);
        }
    }


    private class PhotoAdapter extends RecyclerView.Adapter<PhotoHolder>{
        List<GalleryItem> mGalleryItems;

        public PhotoAdapter(List<GalleryItem> mGalleryItems) {
            this.mGalleryItems = mGalleryItems;
        }
        @NonNull
        @Override
        public PhotoHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(getActivity()).inflate(R.layout.gallert_item,parent,false);
            return new PhotoHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoHolder holder, int position) {
            GalleryItem item = mGalleryItems.get(position);
            //holder.bindGallery();
            mthumnailDownloader.queueThumbnail(holder,item.getmUrl());
        }

        @Override
        public int  getItemCount() {
            return mGalleryItems.size();
        }
    }


}