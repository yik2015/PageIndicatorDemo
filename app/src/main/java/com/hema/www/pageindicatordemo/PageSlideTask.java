package com.hema.www.pageindicatordemo;

import android.os.Handler;
import android.os.Message;
import android.support.v4.view.ViewPager;

/**
 * Created by Administrator on 2016/4/14.
 */
public class PageSlideTask implements Runnable {
    private int position;
    private ViewPager viewPager;
    private int length;

    public PageSlideTask(int position, ViewPager viewPager, int length) {
        this.position = position;
        this.viewPager = viewPager;
        this.length = length;
    }

    @Override
    public void run() {
        position = (position + 1) % length;
        handler.obtainMessage().sendToTarget();
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            viewPager.setCurrentItem(position);
        }
    };
}
