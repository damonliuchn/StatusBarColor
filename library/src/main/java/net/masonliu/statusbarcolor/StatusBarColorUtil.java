package net.masonliu.statusbarcolor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.internal.app.ToolbarActionBar;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

/**
 * Created by liumeng on 2/3/15.
 */
public class StatusBarColorUtil {

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setStatusBarColorResourceWithDrawerLayout(Activity activity, int resource, View menu, DrawerLayout drawerLayout) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            http://stackoverflow.com/questions/26440879/how-do-i-use-drawerlayout-to-display-over-the-actionbar-toolbar-and-under-the-st
//            1、Using Toolbar so that you can embed your action bar into your view hierarchy.
//            2、Making DrawerLayout fitSystemWindows so that it is layed out behind the system bars.
//            3、Disabling Theme.Material's normal status bar coloring so that DrawerLayout can draw there instead.
//            4、drawerLayout.setStatusBarBackgroundColor
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            drawerLayout.setStatusBarBackgroundColor(activity.getResources().getColor(resource));
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(false);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setBackgroundDrawableResource(resource);
        }
        menu.setPadding(0, getStatusBarHeight(activity), 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarColorResourceOnPostCreate(final Activity activity, int resource) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                ViewGroup view = (ViewGroup) activity.findViewById(android.R.id.content);
                View draw = view.getChildAt(0);
                //判断是否有Drawlayout
                if (draw instanceof DrawerLayout) {
                    DrawerLayout drawerLayout = (DrawerLayout) draw;
                    for (int i = 0; i < drawerLayout.getChildCount(); i++) {
                        if (drawerLayout.getChildAt(i) instanceof ScrimInsetsFrameLayout) {
                            View menu = drawerLayout.getChildAt(i);
                            setStatusBarColorResourceWithDrawerLayout(activity, resource, menu, drawerLayout);
                            return;
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setStatusBarColorResourceFromL(activity, resource);
                    } else {
                        Window window = activity.getWindow();
                        window.getDecorView().setFitsSystemWindows(true);
                        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                        tintManager.setStatusBarTintEnabled(true);
                        tintManager.setStatusBarTintResource(resource);

                        final View container = activity.findViewById(android.R.id.content);
                        //container.setFitsSystemWindows(true);
                        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                final int padingTop = getPaddingTop(activity);
                                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                container.setPadding(0, padingTop, 0, 0);
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setStatusBarColorResourceFromL(Activity activity, int resource) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setStatusBarColor(activity.getResources().getColor(resource));
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    public static Toast showToastWithDrawLayout(Context context, CharSequence text, boolean isLong) {
        if (context == null) return null;
        int time = isLong ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, time);
        try {
            if (19 <= Build.VERSION.SDK_INT) {
                toast.getView().setFitsSystemWindows(false);
            }
            toast.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return toast;
    }

    private static int getPaddingTop(Activity activity) {
        ActionBar actionBar = null;
        if (activity instanceof ActionBarActivity) {
            ActionBarActivity actionBarActivity = (ActionBarActivity) activity;
            actionBar = actionBarActivity.getSupportActionBar();
        }
        if (activity instanceof AppCompatActivity) {
            AppCompatActivity actionBarActivity = (AppCompatActivity) activity;
            actionBar = actionBarActivity.getSupportActionBar();
        }
        if (actionBar == null || actionBar instanceof ToolbarActionBar) {
            return getStatusBarHeight(activity);
        }
        return getStatusBarHeight(activity) + actionBar.getHeight();
    }

    private static int getStatusBarHeight(Context context) {
        int result = 0;
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = context.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }
}
