package net.masonliu.statusbarcolor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
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
    private static void setStatusBarColorResource(Activity activity, int resource, View menu, DrawerLayout drawerLayout) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            http://stackoverflow.com/questions/26440879/how-do-i-use-drawerlayout-to-display-over-the-actionbar-toolbar-and-under-the-st
//            1、Using Toolbar so that you can embed your action bar into your view hierarchy.
//            2、Making DrawerLayout fitSystemWindows so that it is layed out behind the system bars.
//            3、Disabling Theme.Material's normal status bar coloring so that DrawerLayout can draw there instead.
//            4、drawerLayout.setStatusBarBackgroundColor
            Window window = activity.getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));

            drawerLayout.setStatusBarBackgroundColor(activity.getResources().getColor(resource));
            int result = 0;
            int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = activity.getResources().getDimensionPixelSize(resourceId);
            }
            menu.setPadding(0, result, 0, 0);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(false);

            Window window = activity.getWindow();
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setBackgroundDrawableResource(resource);

            int result = 0;
            int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                result = activity.getResources().getDimensionPixelSize(resourceId);
            }
            menu.setPadding(0, result, 0, 0);
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarColorResource(final Activity activity, int resource) {

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //判断是否有Drawlayout
                ViewGroup view = (ViewGroup) activity.findViewById(android.R.id.content);
                View draw = view.getChildAt(0);
                if (draw instanceof DrawerLayout) {
                    DrawerLayout drawerLayout = (DrawerLayout) draw;
                    for (int i = 0; i < drawerLayout.getChildCount(); i++) {
                        if (drawerLayout.getChildAt(i) instanceof ScrimInsetsFrameLayout) {
                            View menu = drawerLayout.getChildAt(i);
                            setStatusBarColorResource(activity, resource, menu, drawerLayout);
                            return;
                        }
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                setStatusBarColorResourceFromL(activity, resource);
            } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {

                Window window = activity.getWindow();
                window.getDecorView().setFitsSystemWindows(true);
                window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                        WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                tintManager.setStatusBarTintEnabled(true);
                tintManager.setStatusBarTintResource(resource);
                final View container = activity.findViewById(android.R.id.content);
                //container.setFitsSystemWindows(true);
                if (container != null) {
                    //container.setFitsSystemWindows(true);
                    final int padingTop = checkHaveToolbar(activity) ? tintManager.getConfig().getStatusBarHeight() : tintManager.getConfig().getStatusBarHeight() + tintManager.getConfig().getActionBarHeight();
                    container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            container.setPadding(0, padingTop, 0, 0);
                        }
                    });
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setStatusBarColorResourceFromL(Activity activity, int resource) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            activity.getWindow().setStatusBarColor(activity.getResources().getColor(resource));
        }
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

    private static boolean checkHaveToolbar(Activity activity) {
        //判断是否有Toolbar
        ViewGroup view = (ViewGroup) activity.findViewById(android.R.id.content);
        View draw = view.getChildAt(0);
        if (draw instanceof ViewGroup) {
            ViewGroup drawerLayout = (ViewGroup) draw;
            for (int i = 0; i < drawerLayout.getChildCount(); i++) {
                if (drawerLayout.getChildAt(i) instanceof Toolbar) {
                    return true;
                }
            }
        }
        return false;
    }
}
