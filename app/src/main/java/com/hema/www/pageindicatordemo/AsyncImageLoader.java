package com.hema.www.pageindicatordemo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

/**
 * Created by Administrator on 2016/4/14.
 */
public class AsyncImageLoader {
        private HashMap<String, SoftReference<Drawable>> imageCache;

        public AsyncImageLoader() {
            imageCache = new HashMap<>();
        }

        public interface ImageCallback {
            void imageLoaded(Drawable drawable, String imgUrl);
        }

        public Drawable loadDrawable(final String imgUrl,
                                     final ImageCallback callback) {
            if (imageCache.containsKey(imgUrl)) {
                SoftReference<Drawable> softReference = imageCache.get(imgUrl);

                Drawable drawable = softReference.get();

                if (drawable != null) {
                    callback.imageLoaded(drawable, imgUrl);

                    return drawable;
                }
            }

            final android.os.Handler handler = new android.os.Handler() {
                @Override
                public void handleMessage(Message msg) {
                    callback.imageLoaded((Drawable) msg.obj, imgUrl);
                }
            };

            new Thread() {
                @Override
                public void run() {
                    Drawable drawable = loadImageFromUrl(imgUrl);
                    imageCache.put(imgUrl, new SoftReference<Drawable>(drawable));

                    Message msg = handler.obtainMessage(0, drawable);

                    handler.sendMessage(msg);
                }
            }.start();

            return null;
        }

        public Drawable loadImageFromUrl(String url) {
            Bitmap b;

            try {
                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.connect();

                InputStream inputstream = connection.getInputStream();
                b = BitmapFactory.decodeStream(inputstream);

                return new BitmapDrawable(b);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        public void clearCache() {
            if (imageCache.size() > 0) {
                imageCache.clear();
            }
        }

    }
