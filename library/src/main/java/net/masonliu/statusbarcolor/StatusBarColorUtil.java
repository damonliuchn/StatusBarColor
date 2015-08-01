package net.masonliu.statusbarcolor;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
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

    /* StatusBarColor */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarColorResourceOnPostCreate(final Activity activity, int resource) {
        setStatusBarColorOnPostCreate(activity, activity.getResources().getColor(resource));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setStatusBarColorOnPostCreate(final Activity activity, int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                //判断是否有Drawlayout
                if (hasDrawLayout(activity)) {
                    DrawerLayout drawerLayout = (DrawerLayout) ((ViewGroup) activity.findViewById(android.R.id.content)).getChildAt(0);
                    for (int i = 0; i < drawerLayout.getChildCount(); i++) {
                        if (drawerLayout.getChildAt(i) instanceof ScrimInsetsFrameLayout) {
                            View menu = drawerLayout.getChildAt(i);
                            setStatusBarColorResourceWithDrawerLayout(activity, color, menu, drawerLayout);
                            return;
                        }
                    }
                } else {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        setStatusBarColorResourceFromL(activity, color);
                    } else {
                        Window window = activity.getWindow();
                        window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

                        SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                        tintManager.setStatusBarTintEnabled(true);
                        tintManager.setStatusBarTintColor(color);

                        final View container = activity.findViewById(android.R.id.content);
                        container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                final int padingTop = getPaddingTop(activity);
                                container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                                container.setPadding(0, padingTop, 0, container.getPaddingBottom());
                            }
                        });
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static void setStatusBarColorResourceWithDrawerLayout(Activity activity, int color, View menu, DrawerLayout drawerLayout) {
        Window window = activity.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            http://stackoverflow.com/questions/26440879/how-do-i-use-drawerlayout-to-display-over-the-actionbar-toolbar-and-under-the-st
//            1、Using Toolbar so that you can embed your action bar into your view hierarchy.
//            2、Making DrawerLayout fitSystemWindows so that it is layed out behind the system bars.
//            3、Disabling Theme.Material's normal status bar coloring so that DrawerLayout can draw there instead.
//            4、drawerLayout.setStatusBarBackgroundColor
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(activity.getResources().getColor(android.R.color.transparent));
            drawerLayout.setStatusBarBackgroundColor(color);
        } else if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            drawerLayout.setFitsSystemWindows(false);
            window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setBackgroundDrawable(new ColorDrawable(color));
        }
        menu.setPadding(0, getStatusBarHeight(activity), 0, 0);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setStatusBarColorResourceFromL(Activity activity, int color) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setStatusBarColor(color);
    }

    /* NavigationBarColor */

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setNavigationBarColorResourceOnPostCreate(final Activity activity, int resource) {
        setNavigationBarColorOnPostCreate(activity, activity.getResources().getColor(resource));
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static void setNavigationBarColorOnPostCreate(final Activity activity, int color) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    setNavigationBarColorResourceFromL(activity, color);
                } else {
                    Window window = activity.getWindow();
                    window.setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION, WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);

                    final SystemBarTintManager tintManager = new SystemBarTintManager(activity);
                    if (!tintManager.getConfig().hasNavigtionBar()) {
                        return;
                    }
                    tintManager.setNavigationBarTintEnabled(true);
                    tintManager.setNavigationBarTintColor(color);
                    final View container = activity.findViewById(android.R.id.content);
                    container.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                        @Override
                        public void onGlobalLayout() {
                            int padingBottom = hasDrawLayout(activity) ? 0 : tintManager.getConfig().getNavigationBarHeight();
                            container.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                            container.setPadding(0, container.getPaddingTop(), 0, padingBottom);
                        }
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setNavigationBarColorResourceFromL(Activity activity, int color) {
        activity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        activity.getWindow().setNavigationBarColor(color);
    }

    /* other tool */

    private static boolean hasDrawLayout(Activity activity) {
        ViewGroup view = (ViewGroup) activity.findViewById(android.R.id.content);
        View draw = view.getChildAt(0);
        //判断是否有Drawlayout
        if (draw instanceof DrawerLayout) {
            return true;
        }
        return false;
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
