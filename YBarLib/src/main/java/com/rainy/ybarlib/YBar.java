package com.rainy.ybarlib;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * time:2019-01-10 15:43
 * description:
 *
 * @author yueleilei
 */
public class YBar {
    private static HashMap<String, YBar> mYBarMap = new HashMap<>();
    private static final int ID_STATUS_VIEW = 0xFFFF0001;
    private static final int ID_MASK_VIEW = 0xFFFF0002;
    /**
     * 状态栏颜色 默认白色
     */
    private int mBarColor = 0xFFFFFFFF;
    /**
     * 4.4-6.0由于不支持darkMode
     * 所以在darkMode下添加半透明蒙层
     * 以防看不清状态栏的浅色文字
     */
    private int mMaskViewColor = 0x55000000;
    /**
     * 是否需要空出状态栏高度
     */
    private boolean clipToTop = true;
    /**
     * 状态栏是否需要显示深色字体样式
     */
    private boolean dartMode = true;
    private View mRootView;
    private Window mWindow;
    private Context mContext;

    private YBar(@NonNull Activity activity) {
        mRootView = activity.findViewById(android.R.id.content);
        mWindow = activity.getWindow();
        mContext = activity;
    }

    private YBar(@NonNull Fragment fragment) {
        FragmentActivity activity = fragment.getActivity();
        if (activity != null) {
            mRootView = fragment.getView();
            mWindow = fragment.getActivity().getWindow();
            mContext = activity;
        }
    }

    public static YBar with(@NonNull Activity activity) {
        YBar yBar = mYBarMap.get(activity.getClass().getCanonicalName() + activity.hashCode());
        if (yBar == null) {
            yBar = new YBar(activity);
            mYBarMap.put(activity.getClass().getCanonicalName() + activity.hashCode(), yBar);
        }
        return yBar;
    }

    public static YBar with(@NonNull Fragment fragment) {
        YBar yBar = mYBarMap.get(fragment.getClass().getCanonicalName() + fragment.hashCode());
        if (yBar == null) {
            yBar = new YBar(fragment);
            mYBarMap.put(fragment.getClass().getCanonicalName() + fragment.hashCode(), yBar);
        }
        return yBar;
    }

    public YBar clipToTop(boolean clipToTop) {
        this.clipToTop = clipToTop;
        return this;
    }

    public YBar dartMode(boolean dartMode) {
        this.dartMode = dartMode;
        return this;
    }

    public YBar barColor(@ColorInt int color) {
        this.mBarColor = color;
        return this;
    }

    public YBar maskColor(@ColorInt int color) {
        this.mMaskViewColor = color;
        return this;
    }

    public void init() {
        if (mRootView == null || mWindow == null || mContext == null) return;
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) return;//4.4以下不处理
        processClip();//处理是否需要空出状态栏高度
        processTranslucent();//处理状态栏透明
        processDarkMode();//处理状态栏文字是否为dark模式

    }

    private void processDarkMode() {
        //6.0设置状态栏文字dark模式
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dartMode) {
                BarUtils.setMeizuStatusBarDarkIcon(mWindow, true);
                BarUtils.setMiuiStatusBarDarkMode(mWindow, true);
                mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                BarUtils.setMeizuStatusBarDarkIcon(mWindow, false);
                BarUtils.setMiuiStatusBarDarkMode(mWindow, false);
                mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            }
        } else {//4.4到6.0以下添加状态栏半透明背景
            if (dartMode) {
                View maskView = new View(mContext);
                maskView.setId(ID_MASK_VIEW);
                maskView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BarUtils.getStatusBarHeight(mContext)));
                maskView.setBackgroundColor(mMaskViewColor);
                FrameLayout decorView = (FrameLayout) mWindow.getDecorView();
                decorView.addView(maskView);
            } else {
                FrameLayout decorView = (FrameLayout) mWindow.getDecorView();
                View maskView = decorView.findViewById(ID_MASK_VIEW);
                if (maskView != null) {
                    decorView.removeView(maskView);
                }
            }
        }
    }

    private void processTranslucent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//5.0 全透明实现
            mWindow.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            mWindow.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            mWindow.setStatusBarColor(mBarColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWindow.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    private void processClip() {
        if (clipToTop) {
            mRootView.setPadding(0, BarUtils.getStatusBarHeight(mContext), 0, 0);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//4.4-5.0需要添加个假的
                ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
                View statusView = decorView.findViewById(ID_STATUS_VIEW);
                if (statusView == null) {
                    statusView = new View(mContext);
                    statusView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, BarUtils.getStatusBarHeight(mContext)));
                    statusView.setBackgroundColor(mBarColor);
                    statusView.setId(ID_STATUS_VIEW);
                    decorView.addView(statusView);
                } else {
                    statusView.setBackgroundColor(mBarColor);
                }
            }
        } else {
            mRootView.setPadding(0, 0, 0, 0);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {//移除statusView
                ViewGroup decorView = (ViewGroup) mWindow.getDecorView();
                View statusView = decorView.findViewById(ID_STATUS_VIEW);
                if (statusView != null) {
                    decorView.removeView(statusView);
                }
            }
        }
    }

    /**
     * 一定要在使用的Activity/Fragment onDestroy里调用
     * 防止内存泄漏
     */
    public static void destroy(Object obj) {
        //删除当前界面对应的ImmersionBar对象
        String barKeyName = obj.getClass().getCanonicalName() + obj.hashCode();
        Iterator<Map.Entry<String, YBar>> iterator = mYBarMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, YBar> entry = iterator.next();
            if (entry.getKey().contains(barKeyName) || (entry.getKey().equals(barKeyName))) {
                iterator.remove();
            }
        }
    }
}
