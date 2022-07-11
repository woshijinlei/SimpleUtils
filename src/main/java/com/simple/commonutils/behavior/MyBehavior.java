package com.simple.commonutils.behavior;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.OverScroller;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.appbar.AppBarLayout;
import java.lang.reflect.Field;

public class MyBehavior extends AppBarLayout.Behavior {
    private static final String c = "CustomAppbarLayoutBehavior";

    /* renamed from: d  reason: collision with root package name */
    private static final int f9936d = 1;

    /* renamed from: a  reason: collision with root package name */
    private boolean f9937a;
    private boolean b;

    public MyBehavior(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
    }

    private Field a() throws NoSuchFieldException {
        try {
            return getClass().getSuperclass().getSuperclass().getDeclaredField("mFlingRunnable");
        } catch (NoSuchFieldException unused) {
            return MyBehavior.class.getSuperclass().getSuperclass().getSuperclass().getDeclaredField("flingRunnable");
        }
    }

    private Field b() throws NoSuchFieldException {
        try {
            return getClass().getSuperclass().getSuperclass().getDeclaredField("mScroller");
        } catch (NoSuchFieldException unused) {
            return MyBehavior.class.getSuperclass().getSuperclass().getSuperclass().getDeclaredField("scroller");
        }
    }

    private void e(AppBarLayout appBarLayout) {
        try {
            Field a2 = a();
            Field b2 = b();
            a2.setAccessible(true);
            b2.setAccessible(true);
            Runnable runnable = (Runnable) a2.get(this);
            OverScroller overScroller = (OverScroller) b2.get(this);
            if (runnable != null) {
                appBarLayout.removeCallbacks(runnable);
                a2.set(this, (Object) null);
            }
            if (overScroller != null && !overScroller.isFinished()) {
                overScroller.abortAnimation();
            }
        } catch (NoSuchFieldException e2) {
            e2.printStackTrace();
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
        }
    }

    /* renamed from: c */
    public boolean onInterceptTouchEvent(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, MotionEvent motionEvent) {
        this.b = false;
        if (this.f9937a) {
            this.b = true;
        }
        if (motionEvent.getActionMasked() == 0) {
            e(appBarLayout);
        }
        return super.onInterceptTouchEvent(coordinatorLayout, appBarLayout, motionEvent);
    }

    /* renamed from: d */
    public void onNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int i3, int i4, int i5) {
        if (!this.b) {
            super.onNestedScroll(coordinatorLayout, appBarLayout, view, i, i2, i3, i4, i5);
        }
    }

    public void onNestedPreScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i, int i2, int[] iArr, int i3) {
        if (i3 == 1) {
            this.f9937a = true;
        }
        if (!this.b) {
            super.onNestedPreScroll(coordinatorLayout, appBarLayout, view, i, i2, iArr, i3);
        }
    }

    public boolean onStartNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, View view2, int i, int i2) {
        e(appBarLayout);
        return super.onStartNestedScroll(coordinatorLayout, appBarLayout, view, view2, i, i2);
    }

    public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, AppBarLayout appBarLayout, View view, int i) {
        super.onStopNestedScroll(coordinatorLayout, appBarLayout, view, i);
        this.f9937a = false;
        this.b = false;
    }
}
