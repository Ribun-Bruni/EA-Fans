package com.ribunbruni.eafans;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.tumblr.jumblr.types.Photo;
import com.tumblr.jumblr.types.PhotoPost;
import com.tumblr.jumblr.types.PhotoSize;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;

public class PhotoDownloader {
    // Edit me to whatever blog you want
    final static String BLOG_NAME = "elsanna-art-archive";

    // No need to edit anything below!
    public static void main(String[] args) throws IOException {
        final Gson gson = new Gson();
        // https://stackoverflow.com/a/5554296/13668740
        Type postListType = new TypeToken<List<PhotoPost>>() {
        }.getType();
        // Read JSON that we obtained from PostsArchiver
        // Warning: We assume that all posts are PhotoPosts!
        List<PhotoPost> postList = gson.fromJson(new FileReader(BLOG_NAME + ".json"), postListType);

        // Directory for downloading photos
        File photosDir = new File(BLOG_NAME + "-photos");

        int counter = 0;
        // Loop through all posts
        for (PhotoPost post : postList) {
            counter++;
            List<Photo> photos = post.getPhotos();
            // First check that there are photos in the current post!
            if (photos == null) continue;

            // Create directory for post
            File postDir = new File(photosDir.getPath() + "/" + post.getId() + "/");
            // This should allow us to resume where we left off from
            if (postDir.exists()) continue;
            // Make the directory
            if (!postDir.mkdirs()) {
                System.err.println("Unable to create dir for " + post.getPostUrl());
                continue;
            }

            // Loop through all photos in the post
            for (Photo photo : photos) {
                PhotoSize originalSize = photo.getOriginalSize();
                // Create the post's File location in memory
                File photoFile =
                        new File (postDir.getPath() + "/" + originalSize.getWidth() + "x" + originalSize.getHeight() + "." + FilenameUtils.getExtension(originalSize.getUrl()));

                // Extra setup for downloading the photo
                // Tumblr is weird in that without this header, it will give you some bloated HTML page along with the photo
                URL url = new URL(originalSize.getUrl());
                URLConnection connection = url.openConnection();
                connection.setRequestProperty("Accept", "image/avif,image/webp,*/*");

                // Now download this photo into our file
                FileUtils.copyInputStreamToFile(
                        connection.getInputStream(),
                        photoFile);
            }
            System.out.println("Processed " + counter + " posts.");
        }
    }
}
