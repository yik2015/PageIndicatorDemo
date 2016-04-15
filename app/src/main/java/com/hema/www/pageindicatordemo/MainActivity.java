package com.hema.www.pageindicatordemo;

import android.annotation.TargetApi;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

    private ImageView naviImg;
    private View item;
    private MyAdapter adapter;

    private ImageView[] indicator_imgs = new ImageView[7];

    private int curIdx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        inflater = LayoutInflater.from(this);

        viewPager = (ViewPager) findViewById(R.id.view_pager);
        List<View> list = new ArrayList<>();

        for (int i = 0; i < indicator_imgs.length; i++) {
            item = inflater.inflate(R.layout.item, null);
            ((TextView) item.findViewById(R.id.text)).setText("ViewPager # " + i);
            list.add(item);
        }

        adapter = new MyAdapter(list);

        viewPager.setAdapter(adapter);

        viewPager.addOnPageChangeListener(this);

        initIndicator();
    }

    private void initIndicator() {
        ImageView imgView;
        View v = findViewById(R.id.page_indicator);

        for (int i = 0; i < indicator_imgs.length; i++) {
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
        curIdx = position;

        for (int i = 0; i < indicator_imgs.length; i++) {
            indicator_imgs[i].setBackgroundResource(R.drawable.page_indicator_unfocused);
        }

        indicator_imgs[position].setBackgroundResource(R.drawable.page_indicator_focused);
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    protected void onResume() {
        super.onResume();

        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        scheduledExecutorService.scheduleWithFixedDelay(new PageSlideTask(curIdx, viewPager,
                indicator_imgs.length), 10, 10, TimeUnit.SECONDS);
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
                            naviImg = (ImageView) view.findViewById(R.id.image);
                            naviImg.setBackground(drawable);

                            container.removeView(mList.get(position));
                            container.addView(mList.get(position));
                        }
                    });

            View view = mList.get(position);
            naviImg = (ImageView) view.findViewById(R.id.image);
            naviImg.setBackground(cachedImg);

            container.removeView(mList.get(position));
            container.addView(mList.get(position));

            return mList.get(position);
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mList.get(position));
        }
    }

}
