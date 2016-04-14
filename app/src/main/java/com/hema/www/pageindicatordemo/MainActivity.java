package com.hema.www.pageindicatordemo;

import android.annotation.TargetApi;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private ViewPager viewPager;

    private LayoutInflater inflater;

    String[] urls = new String[]{"http://a.hiphotos.baidu.com/image/pic/item/3bf33a87e950352ad6465dad5143fbf2b2118b6b.jpg",
            "http://a.hiphotos.baidu.com/image/pic/item/c8177f3e6709c93d002077529d3df8dcd0005440.jpg",
            "http://f.hiphotos.baidu.com/image/pic/item/7aec54e736d12f2ecc3d90f84dc2d56285356869.jpg",
            "http://e.hiphotos.baidu.com/image/pic/item/9c16fdfaaf51f3de308a87fc96eef01f3a297969.jpg",
            "http://d.hiphotos.baidu.com/image/pic/item/f31fbe096b63f624b88f7e8e8544ebf81b4ca369.jpg",
            "http://h.hiphotos.baidu.com/image/pic/item/11385343fbf2b2117c2dc3c3c88065380cd78e38.jpg",
            "http://c.hiphotos.baidu.com/image/pic/item/3801213fb80e7bec5ed8456c2d2eb9389b506b38.jpg"};

    private ImageView image;
    private View item;
    private MyAdapter adapter;

    private ImageView[] indicator_imgs = new ImageView[7];

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflater = LayoutInflater.from(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        List<View> list = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            item = inflater.inflate(R.layout.item, null);
            ((TextView) item.findViewById(R.id.text)).setText("ViewPager # " + i);
            list.add(item);
        }

        adapter = new MyAdapter(list);

        viewPager.setAdapter(adapter);

//        viewPager.setOnPageChangeListener(new MyListener());
        viewPager.addOnPageChangeListener(this);

        initIndicator();
    }

    private void initIndicator() {
        ImageView imgView;
        View v = findViewById(R.id.page_indicator);

        for (int i = 0; i < 7; i++) {
            imgView = new ImageView(this);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(10, 10);
            params.setMargins(7, 10, 7, 10);

            imgView.setLayoutParams(params);

            indicator_imgs[i] = imgView;

            if (i == 0) {
                indicator_imgs[i].setBackgroundResource(R.drawable.page_indicator_focused);
            } else {
                indicator_imgs[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
            }

            ((ViewGroup) v).addView(indicator_imgs[i]);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {

        for (int i = 0; i < indicator_imgs.length; i++) {
            indicator_imgs[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
        }

        indicator_imgs[position].setBackgroundResource(R.drawable.page_indicator_focused);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private class MyAdapter extends PagerAdapter {
        private List<View> mList;

        private AsyncImageLoader imageLoader;

        public MyAdapter(List<View> list) {
            mList = list;
            imageLoader = new AsyncImageLoader();
        }

        @Override

        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        @Override
        public Object instantiateItem(final ViewGroup container, final int position) {
            final Drawable cachedImg = imageLoader.loadDrawable(urls[position],
                    new AsyncImageLoader.ImageCallback() {
                        @Override
                        public void imageLoaded(Drawable drawable, String imgUrl) {
                            View view = mList.get(position);
                            image = (ImageView) view.findViewById(R.id.image);
                            image.setBackground(drawable);

                            container.removeView(mList.get(position));
                            container.addView(mList.get(position));

                        }
                    });

            View view = mList.get(position);
            image = (ImageView) view.findViewById(R.id.image);
            image.setBackground(cachedImg);

            container.removeView(mList.get(position));
            container.addView(mList.get(position));

            return mList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }
    }


    static class AsyncImageLoader {
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

}
