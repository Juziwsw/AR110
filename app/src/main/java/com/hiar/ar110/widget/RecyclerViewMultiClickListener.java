package com.hiar.ar110.widget;

import android.content.Context;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;

import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

public class RecyclerViewMultiClickListener implements RecyclerView.OnItemTouchListener {
    public interface OnItemClickListener {
        void onItemClick(View view, int position);

        void onItemDoubleClick(View view, int position);

        void onItemLongClick(View view, int position);
    }

    private RecyclerViewMultiClickListener.OnItemClickListener mListener;
    private GestureDetectorCompat mGestureDetector;

    public RecyclerViewMultiClickListener(Context context, final RecyclerView recyclerView,
                                          RecyclerViewMultiClickListener.OnItemClickListener listener) {
        mListener = listener;
        mGestureDetector = new GestureDetectorCompat(context,
                new GestureDetector.SimpleOnGestureListener() {

                    @Override
                    public boolean onSingleTapConfirmed(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && mListener != null) {
                            mListener.onItemClick(childView, recyclerView.getChildLayoutPosition(childView));
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public boolean onDoubleTap(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && mListener != null) {
                            mListener.onItemDoubleClick(childView, recyclerView.getChildLayoutPosition(childView));
                            return true;
                        }
                        return false;
                    }

                    @Override
                    public void onLongPress(MotionEvent e) {
                        View childView = recyclerView.findChildViewUnder(e.getX(), e.getY());
                        if (childView != null && mListener != null) {
                            mListener.onItemLongClick(childView, recyclerView.getChildLayoutPosition(childView));
                        }
                    }

                    @Override
                    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                        return false;
                    }
                });
    }

    @Override
    public boolean onInterceptTouchEvent(@NotNull RecyclerView rv, @NotNull MotionEvent e) {
        return mGestureDetector.onTouchEvent(e);
    }

    @Override
    public void onTouchEvent(@NotNull RecyclerView rv, @NotNull MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    public static class SimpleOnItemClickListener implements RecyclerViewMultiClickListener.OnItemClickListener {

        @Override
        public void onItemClick(View view, int position) {

        }

        @Override
        public void onItemDoubleClick(View view, int position) {

        }

        @Override
        public void onItemLongClick(View view, int position) {

        }
    }
}
