package com.heima.utils.thread;

import com.heima.model.wemedia.pojos.WmUser;

public class WmThreadLocalUtil {

    private final static ThreadLocal<WmUser> WM_USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 添加用户（存入线程）
     * @param wmUser
     */
    public static void setUser(WmUser wmUser){
        WM_USER_THREAD_LOCAL.set(wmUser);
    }

    /**
     * 获取用户（从线程中获取）
     * @return
     */
    public static WmUser getUser(){
        return  WM_USER_THREAD_LOCAL.get();
    }

    /**
     * 仅会移除当前线程上下文中存储的 WmUser 值，并不会影响其他线程中存储的值
     */
    public static void clear(){
        WM_USER_THREAD_LOCAL.remove();
    }


}
