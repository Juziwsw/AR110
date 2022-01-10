package com.hiar.mybaselib.utils;

import android.view.View;

import com.jakewharton.rxbinding2.view.RxView;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

/**
 * description : TODO:类的作用
 * author : cuiqingchao
 * date : 2020/7/28 10:43
 */
public class KeyUtil {

    /**
     * 防止重复点击
     *
     * @param target 目标view
     * @param listener 监听器
     */
    public static void preventRepeatedClick(final View target, final View.OnClickListener listener) {
        RxView.clicks(target)
                .throttleFirst(1, TimeUnit.SECONDS)
                .subscribe(new Observer<Object>() {

                    @Override
                    public void onSubscribe(Disposable d) {

                    }

                    @Override
                    public void onNext(Object o) {
                        listener.onClick(target);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
