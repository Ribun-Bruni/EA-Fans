package com.ribunbruni.eafans;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tumblr.jumblr.JumblrClient;
import com.tumblr.jumblr.types.Blog;
import com.tumblr.jumblr.types.Post;
import com.tumblr.jumblr.types.User;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
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

//        String blogName = "elsanna-art-archive";
        String blogName = "elsanna-art-archive-explicit";
        String blogUrl = blogName + ".tumblr.com";
        Blog artArchiveBlog = client.blogInfo(blogUrl);

        final List<Post> totalPosts = new ArrayList<>();
        final Map<String, Integer> options = new HashMap<>();
        options.put("limit", 50);

        List<Post> postsTemp;
        int offset = 0;

        do {
            options.put("offset", offset);
            postsTemp = artArchiveBlog.posts(options);
            for (Post post : postsTemp) {
                post.setClient(null);
                totalPosts.add(post);
            }
            System.out.println("Processed " + offset + " posts.");
            offset += postsTemp.size(); // Should be 50
        } while (!postsTemp.isEmpty());

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
        FileWriter fileWriter = new FileWriter(blogName + ".json");
        gson.toJson(totalPosts, fileWriter);
        fileWriter.flush();
        fileWriter.close();
    }

}
