package com.ribunbruni.eafans;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PostsArchiver {
    // Edit me to whatever blog you want
    final static String BLOG_NAME = "elsanna-art-archive";

    // No need to edit anything below!
    final static String BLOG_URL = BLOG_NAME + ".tumblr.com";

    public static void main(String[] args) throws IOException {
        // Create a new client
        /* Env vars:
        CONSUMER_KEY
        CONSUMER_SECRET
        TOKEN
        TOKEN_SECRET
         */

        // Authenticate via OAuth
        JumblrClient client = new JumblrClient(
                System.getenv("CONSUMER_KEY"),
                System.getenv("CONSUMER_SECRET")
        );
        client.setToken(
                System.getenv("TOKEN"),
                System.getenv("TOKEN_SECRET")
        );

        // Define our blog
        Blog artArchiveBlog = client.blogInfo(BLOG_URL);

        // Prepare a list to contain all the posts
        final List<Post> totalPosts = new ArrayList<>();
        // Set some custom options for our Tumblr requests
        final Map<String, Integer> options = new HashMap<>();
        // 50 is the maximum # of posts Tumblr allows us to fetch at once
        options.put("limit", 50);
        // There's also the "offset" option which is defined below
        // This will let Tumblr know how far back to start fetching posts, for pagination

        // This will contain the temporary 50 posts before we add them to the big list
        List<Post> postsTemp;
        // Offset will start at 0 (the latest posts)
        int offset = 0;

        // Everything in here will be looped until we've processed all posts in the whole blog
        do {
            // Set our offset. Starts at 0, then counts by 50s. 0, 50, 100, 150...
            options.put("offset", offset);
            // Make the request to fetch the next 50 posts
            postsTemp = artArchiveBlog.posts(options);
            // Loop through each post in the list of 50
            for (Post post : postsTemp) {
                // Remove the client (that would cause recursion + stack overflow when formatting to JSON)
                post.setClient(null);
                // Add it to our total list
                totalPosts.add(post);
            }
            System.out.println("Processed " + offset + " posts.");
            // Should be 50, but for the last request, it could be like 37 or something
            offset += postsTemp.size();
        } while (!postsTemp.isEmpty()); // If postsTemp is empty, we've reached the end of the blog

        // Custom gson object, we don't care whether the Tumblr account we logged in as has liked each post.
        final Gson gson = new GsonBuilder()
                .addSerializationExclusionStrategy(new ExclusionStrategy() {
                    @Override
                    public boolean shouldSkipField(FieldAttributes f) {
                        // Skip "liked" field
                        return f.getName().equals("liked");
                    }

                    @Override
                    public boolean shouldSkipClass(Class<?> clazz) {
                        return false;
                    }
                })
                .create();
        // We're going to write to BLOG_NAME.json. Ex: elsanna-art-archive.json
        FileWriter fileWriter = new FileWriter(BLOG_NAME + ".json");
        // Convert our list to JSON and write it
        gson.toJson(totalPosts, fileWriter);
        // Save
        fileWriter.flush();
        fileWriter.close();
    }

}
