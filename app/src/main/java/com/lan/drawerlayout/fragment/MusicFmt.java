package com.lan.drawerlayout.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lan.drawerlayout.R;
import com.lan.drawerlayout.damain.MusicApp;
import com.lan.drawerlayout.utils.ConstantUtil;
import com.lan.drawerlayout.utils.CustomDialog;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by Administrator on 2015/09/26.
 */
public class MusicFmt extends Fragment {


    private ProgressDialog progressDialog;

    private static String[] imageFormatSet = new String[]{".mp3"};

    public static final String fileName = "musicList.txt";


    private PlayerReceiver playerReceiver;


    private int currentItem;


    private List<String> audioList = new ArrayList<String>();
    private LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();


    private ListView listView;

    private SeekBar voiceSeekBar, musicSeekBar;
    private TextView currentProgress, finalProgress;
    private Button playMusic, preMusic, nextMusic, playQueue;
    private ImageButton ibtn_player_voice;

    private ArrayAdapter<String> adapter = null;


    private int currentDuration;

    private boolean isPlaying, isPause;


    RelativeLayout ll_player_voice;    //音量控制面板布局

    // 音量面板显示和隐藏动画
    private Animation showVoicePanelAnimation;
    private Animation hiddenVoicePanelAnimation;

    private AudioManager am;        //音频管理引用，提供对音频的控制

    int currentVolume, maxVolume;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        File file = new File(getActivity().getFilesDir(), fileName);


        if (!file.exists()) {

            new writeFileName().execute();
        } else {

            new getFileName().execute();
        }

        adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, audioList);


        //音量调节面板显示和隐藏的动画
        showVoicePanelAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.push_up_in);
        hiddenVoicePanelAnimation = AnimationUtils.loadAnimation(getActivity(), R.anim.push_up_out);


        playerReceiver = new PlayerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MusicApp.MUSIC_CURRENT);
        intentFilter.addAction(MusicApp.MUSIC_DURATION);
        getActivity().registerReceiver(playerReceiver, intentFilter);

        am = (AudioManager) getActivity().getSystemService(Context.AUDIO_SERVICE);
        currentVolume = am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        am.setStreamVolume(AudioManager.STREAM_MUSIC, currentVolume, 0);


        Log.w("MusicFmt", "OnCreate()");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(playerReceiver);
        Log.w("MusicFmt", "onDestroy()");
        onSaveInstanceState(new Bundle());
    }

    public void findViewById(View view) {

        voiceSeekBar = (SeekBar) view.findViewById(R.id.sb_player_voice);
        voiceSeekBar.setProgress(currentVolume);
        voiceSeekBar.setMax(maxVolume);

        SeekBarChangeListener seekBarChangeListener = new SeekBarChangeListener();

        voiceSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);

        listView = (ListView) view.findViewById(R.id.listView);
        musicSeekBar = (SeekBar) view.findViewById(R.id.audioTrack);

        musicSeekBar.setOnSeekBarChangeListener(seekBarChangeListener);
        currentProgress = (TextView) view.findViewById(R.id.current_progress);
        finalProgress = (TextView) view.findViewById(R.id.final_progress);
        playMusic = (Button) view.findViewById(R.id.play_music);
        nextMusic = (Button) view.findViewById(R.id.next_music);
        preMusic = (Button) view.findViewById(R.id.previous_music);
        playQueue = (Button) view.findViewById(R.id.play_queue);
        ibtn_player_voice = (ImageButton) view.findViewById(R.id.ibtn_player_voice);
        ll_player_voice = (RelativeLayout) view.findViewById(R.id.ll_player_voice);


    }

    public void setViewOnclickListener() {
        viewOnclickListener viewOnclickListener = new viewOnclickListener();
        playMusic.setOnClickListener(viewOnclickListener);
        nextMusic.setOnClickListener(viewOnclickListener);
        preMusic.setOnClickListener(viewOnclickListener);
        ibtn_player_voice.setOnClickListener(viewOnclickListener);

    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.music_main_my, container, false);

        findViewById(rootView);

        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new MusicItemClickListener());

        listView.setOnCreateContextMenuListener(new View.OnCreateContextMenuListener() {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {

                Vibrator vibrator = (Vibrator) getActivity().getSystemService(Service.VIBRATOR_SERVICE);
                vibrator.vibrate(50); // 长按振动
                final AdapterView.AdapterContextMenuInfo menuInfo2 = (AdapterView.AdapterContextMenuInfo) menuInfo;

                musicListItemDialog(menuInfo2.position); // 长按后弹出的对话框


            }
        });



        setViewOnclickListener();


        if (savedInstanceState != null) {
            String finalPro = savedInstanceState.getString("finalTime");
            finalProgress.setText(finalPro);
            Log.w("MusicFmt", "finalPro is :"+finalPro);


        }

        Log.w("MusicFmt", "onCreateView()");

        return rootView;


    }

    public void musicListItemDialog(final int whereIs)
    {
        String[] menuItems = new String[]{"删除音乐", "设为铃声", "查看详情"};
        ListView menuList = new ListView(getActivity());
        menuList.setCacheColorHint(Color.TRANSPARENT);
        menuList.setDividerHeight(1);
        menuList.setAdapter(new ArrayAdapter<String>(getActivity(),
                R.layout.context_dialog_layout, R.id.dialogText, menuItems));
        menuList.setLayoutParams(new ViewGroup.LayoutParams(ConstantUtil
                .getScreen(getActivity())[0] / 2, ViewGroup.LayoutParams.WRAP_CONTENT));

        final CustomDialog customDialog = new CustomDialog.Builder(
                getActivity()).setTitle(R.string.operation)
                .setView(menuList).create();
        customDialog.show();

        menuList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    final String musicName = audioList.get(whereIs);
                    final String path = map.get(musicName);
                    AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
                    dialog.setTitle("操作");
                    dialog.setMessage("确认删除" + musicName + "?");
                    dialog.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            File file = new File(path);
                            if (file.exists()) {
                                file.delete();
                                audioList.remove(whereIs);
                                map.remove(musicName);
                                adapter.notifyDataSetChanged();

                            }


                            customDialog.dismiss();

                        }
                    });

                    dialog.setNegativeButton("取消", null);
                    dialog.setCancelable(true);
                    dialog.show();
                }
            }
        });
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        String si = finalProgress.getText().toString();
        outState.putString("finalTime", si);
        Log.w("MusicFmt", "onSaveInstanceState()");
    }




    @Override
    public void onViewStateRestored(Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
//        String si=savedInstanceState.getString("finalTime");
//        finalProgress.setText(si);
//        Log.w("MusicFmt", "finalPro is :" + si);


    }

    public void playMusic(int position) {

        Intent playIntent = new Intent(MusicApp.launchService);
        playIntent.putExtra("currentItem", position);
        playIntent.putExtra("MSG", MusicApp.PLAY_MUSIC);
        getActivity().startService(playIntent);
        isPlaying = true;
        isPause = false;

    }


    class MusicItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            currentItem = position;
            playMusic(currentItem);
        }
    }


    class viewOnclickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {


            switch (v.getId()) {
                case R.id.play_music: {

                    if (isPlaying) {
                        Intent intent = new Intent();
                        intent.setAction(MusicApp.launchService);
                        intent.putExtra("MSG", MusicApp.PAUSE_MUSIC);
                        getActivity().startService(intent);
                        playMusic.setBackgroundResource(R.drawable.play_selector);

//                        getActivity().sendBroadcast(intent);
                        isPlaying = false;
                        isPause = true;

                        Log.w("MusicFmt", "after judge  isPlaying is:" + isPlaying);
                        Log.w("MusicFmt", "after judge  isPause is:" + isPause);


                    } else if (isPause) {
                        Intent intentP = new Intent();
                        intentP.setAction(MusicApp.launchService);
                        playMusic.setBackgroundResource(R.drawable.play_selector);

//                        intent.putExtra("currentItem", currentItem);
//                        intentP.putExtra("currentDuration",currentDuration);
                        intentP.putExtra("MSG", MusicApp.COTINUNE_MUSIC);
                        getActivity().startService(intentP);
//                        getActivity().sendBroadcast(intentP);

                        isPlaying = true;
                        isPause = false;

                        Log.w("MusicFmt", "after judge  isPlaying is:" + isPlaying);
                        Log.w("MusicFmt", "after judge  isPause is:" + isPause);
                    }
                    break;

                }
                case R.id.previous_music: {
                    currentItem--;
                    if (currentItem < 0) {
                        currentItem = audioList.size() - 1;
                    }

                    Toast.makeText(getActivity(), audioList.get(currentItem), Toast.LENGTH_SHORT).show();

                    Intent preInt = new Intent(MusicApp.launchService);
                    preInt.putExtra("currentItem", currentItem);
                    preInt.putExtra("MSG", MusicApp.PLAY_MUSIC);
                    getActivity().startService(preInt);
                    break;
                }
                case R.id.next_music: {
                    currentItem++;
                    if (currentItem > audioList.size() - 1) {
                        currentItem = 0;
                        Toast.makeText(getActivity(), audioList.get(currentItem), Toast.LENGTH_SHORT).show();

                    }
                    Toast.makeText(getActivity(), audioList.get(currentItem), Toast.LENGTH_SHORT).show();

                    Intent nexInt = new Intent(MusicApp.launchService);
                    nexInt.putExtra("currentItem", currentItem);
                    nexInt.putExtra("MSG", MusicApp.PLAY_MUSIC);
                    getActivity().startService(nexInt);
                    break;
                }

                case R.id.ibtn_player_voice: {
                    Log.w("MusicFmt", "next voicePanelAnimation()");
                    voicePanelAnimation();
                    break;
                }


            }


        }
    }


    //控制显示音量控制面板的动画
    public void voicePanelAnimation() {
        if (ll_player_voice.getVisibility() == View.GONE) {
            ll_player_voice.startAnimation(showVoicePanelAnimation);
            ll_player_voice.setVisibility(View.VISIBLE);
        } else {
            ll_player_voice.startAnimation(hiddenVoicePanelAnimation);
            ll_player_voice.setVisibility(View.GONE);
        }
    }

    public void audioTrackChange(int progress) {
        Intent i = new Intent(MusicApp.launchService);
        i.putExtra("MSG", MusicApp.PROGRESS_CHAGE);
        i.putExtra("currentDuration", progress);
        getActivity().startService(i);
    }


    class SeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            switch (seekBar.getId()) {
                case R.id.audioTrack:
                    if (fromUser) {
                        currentDuration = progress;
                        audioTrackChange(progress); // 用户控制进度的改变
                    }
                    break;
                case R.id.sb_player_voice:
                    // 设置音量
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, progress, AudioManager.FLAG_PLAY_SOUND);


                    break;
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    class writeFileName extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {

            progressDialog = new ProgressDialog(getActivity());

            progressDialog.setMessage("正在加载歌曲...");

            progressDialog.setTitle("请稍等");
            progressDialog.setCancelable(false);
            progressDialog.show();

        }

        @Override
        protected Void doInBackground(Void... params) {

            getFiles("/sdcard/");

            return null;
        }


        protected void getFiles(String path) {
            File files = new File(path);
            File[] file = files.listFiles();


            try {
                for (File f : file) {
                    if (f.isDirectory()) {
                        getFiles(f.getAbsolutePath());
                    } else {
                        if (isAudioFile(f.getPath())) {
                            writeMusicName(f.getPath());
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        protected synchronized void writeMusicName(String path) {
            try {
                FileOutputStream fos = getActivity().openFileOutput(fileName, Context.MODE_APPEND);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                bw.write(path);

                audioList.add(path.substring(path.lastIndexOf('/') + 1));
                map.put(path.substring(path.lastIndexOf('/') + 1), path);

                bw.newLine();
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        protected boolean isAudioFile(String path) {
            for (String format : imageFormatSet) {
                if (path.endsWith(format)) {
                    return true;
                }
            }
            return false;
        }


        @Override
        protected void onPostExecute(Void aVoid) {

            progressDialog.dismiss();

        }
    }


    class getFileName extends AsyncTask<Void, Void, Void> {


        @Override
        protected void onPreExecute() {
            progressDialog = new ProgressDialog(getActivity());

            progressDialog.setMessage("正在获取歌曲列表...");
            progressDialog.setTitle("请稍等");
            progressDialog.setCancelable(false);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {

            try {

                FileInputStream fis = getActivity().openFileInput(fileName);
                BufferedReader br = new BufferedReader(new InputStreamReader(fis));

                String line = "";

                while ((line = br.readLine()) != null) {
                    map.put(line.substring(line.lastIndexOf("/") + 1), line);
                    audioList.add(line.substring(line.lastIndexOf('/') + 1));
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void LinkedMap) {
            progressDialog.dismiss();

        }
    }


    class PlayerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();

            if (action.equals(MusicApp.MUSIC_DURATION)) {
                int duration = intent.getIntExtra("totalDuration", -1);
                musicSeekBar.setMax(duration);
                finalProgress.setText(MusicApp.formatTime(duration));
            } else if (action.equals(MusicApp.MUSIC_CURRENT)) {
                currentDuration = intent.getIntExtra("currentDuration", -1);
                currentProgress.setText(MusicApp.formatTime(currentDuration));
                musicSeekBar.setProgress(currentDuration);
            }


        }
    }


}
