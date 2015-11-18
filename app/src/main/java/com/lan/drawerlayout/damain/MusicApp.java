package com.lan.drawerlayout.damain;

/**
 * Created by Administrator on 2015/09/27.
 */
public class MusicApp {

    public static final int PLAY_MUSIC=1;
    public static final int PAUSE_MUSIC=2;
    public static final int COTINUNE_MUSIC=3;
    public static final int PROGRESS_CHAGE=4;

    public static final String launchService="com.lan.media.MUSIC_SERVICE";

    public static final String fileName="musicList.txt";

    public static final String MUSIC_DURATION = "com.lan.action.MUSIC_DURATION";//新音乐长度更新动作

    public static final String MUSIC_CURRENT = "com.lan.action.MUSIC_CURRENT";	//当前音乐播放时间更新动作




    public static String formatTime(long time) {
        String min = time / (1000 * 60) + "";
        String sec = time % (1000 * 60) + "";
        if (min.length() < 2) {
            min = "0" + time / (1000 * 60) + "";
        } else {
            min = time / (1000 * 60) + "";
        }
        if (sec.length() == 4) {
            sec = "0" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 3) {
            sec = "00" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 2) {
            sec = "000" + (time % (1000 * 60)) + "";
        } else if (sec.length() == 1) {
            sec = "0000" + (time % (1000 * 60)) + "";
        }
        return min + ":" + sec.trim().substring(0, 2);
    }

}
