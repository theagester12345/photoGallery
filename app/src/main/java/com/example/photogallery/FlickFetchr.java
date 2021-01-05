package com.example.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class FlickFetchr {
    private static final String TAG ="FlickrFetchr";
    private static final String API_KEY = "35c3bcc9b39e7287b62c00fa717670fa";
    private static final String FETCH_METHOD = "flickr.photos.getRecent";
    private static final String SEARCH_METHOD="flickr.photos.search";
    private static final Uri ENDPOINT = Uri.parse("https://api.flickr.com/services/rest/")
            .buildUpon()
            .appendQueryParameter("api_key",API_KEY)
            .appendQueryParameter("format","json")
            .appendQueryParameter("nojsoncallback","1")
            .appendQueryParameter("extras","url_s")
            .build();

    public byte[] getUrlBytes (String url) throws IOException {
        URL mUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) mUrl.openConnection();

        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = httpURLConnection.getInputStream();

            if (httpURLConnection.getResponseCode()!= HttpURLConnection.HTTP_OK){
                throw new IOException(httpURLConnection.getResponseMessage()+": with "+url);
            }
            int byteReader = 0 ;
            byte[] buffer = new byte[1024];
            while ((byteReader=in.read(buffer))>0){
                out.write(buffer,0,byteReader);
            }

            out.close();
            return out.toByteArray();
        }finally {
            httpURLConnection.disconnect();;

        }
    }

    public String getUrlString (String url) throws IOException{
        return new String(getUrlBytes(url));
    }

    public List<GalleryItem> DownloadGallery(String urls){
        List<GalleryItem> items = new ArrayList<>();

        try {
         /*   String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method","flickr.photos.getRecent")
                    .appendQueryParameter("api_key",API_KEY)
                    .appendQueryParameter("format","json")
                    .appendQueryParameter("nojsoncallback","1")
                    .appendQueryParameter("extras","url_s")
                    .build().toString();*/

            String result = new FlickFetchr().getUrlString(urls);
            JSONObject body = new JSONObject(result);
            parseItems(items,body);
            Log.i(TAG,result);
        } catch (IOException e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(TAG,e.getMessage());
        }

        return items;
    }

    private String buildUrl (String method, String query){
        Uri.Builder builder = ENDPOINT.buildUpon().appendQueryParameter("method",method);

        //add Query to param for search
        if (method.equals(SEARCH_METHOD)){
            builder.appendQueryParameter("text",query);
        }

        return builder.build().toString();
    }


    public List<GalleryItem> fetchRecent_photos (){
        String url = buildUrl(FETCH_METHOD,null);
        return DownloadGallery(url);
    }

    public List<GalleryItem> SearchPhoto (String query){
        String url = buildUrl(SEARCH_METHOD,query);
        return DownloadGallery(url);
    }

    public void parseItems (List<GalleryItem> items, JSONObject jsonBody) throws JSONException{
        JSONObject photosObj = jsonBody.getJSONObject("photos");
        JSONArray photoArray = photosObj.getJSONArray("photo");

        //Loop through array
        // attach to model
        //Parse to ArrayList
        for (int i =0 ; i<photoArray.length();i++){
            JSONObject arrayObjs = photoArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setmId(arrayObjs.getString("id"));
            item.setmCaption(arrayObjs.getString("title"));

            //check if arrayobj has url
            //if not ignore and search for that has
            if (!arrayObjs.has("url_s")){
                continue;
            }

            item.setmUrl(arrayObjs.getString("url_s"));
            items.add(item);


        }
    }
}
