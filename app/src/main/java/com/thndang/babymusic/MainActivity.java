package com.thndang.babymusic;

import android.app.Activity;
import android.content.res.AssetFileDescriptor;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.WindowInsets;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

    private static final int BG = Color.rgb(248, 245, 238);
    private static final int CARD = Color.WHITE;
    private static final int TEXT = Color.rgb(42, 42, 51);
    private static final int MUTED = Color.rgb(122, 123, 133);
    private static final int ACCENT = Color.rgb(111, 99, 246);
    private static final int BORDER = Color.rgb(232, 226, 216);

    private final List<Song> songs = new ArrayList<>();
    private final List<Button> playButtons = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private int currentIndex = -1;
    private boolean isPrepared = false;

    private LinearLayout playerBar;
    private TextView nowTitle;
    private TextView nowArtist;
    private Button playerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setStatusBarColor(BG);
        getWindow().setNavigationBarColor(BG);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(BG);

        root.setOnApplyWindowInsetsListener((view, insets) -> {
            int top = insets.getSystemWindowInsetTop();
            int bottom = insets.getSystemWindowInsetBottom();
            view.setPadding(0, top, 0, bottom);
            return insets;
        });

        TextView title = new TextView(this);
        title.setText("🎵 Nhạc Cho Bé");
        title.setTextColor(TEXT);
        title.setTextSize(31);
        title.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        title.setPadding(dp(18), dp(18), dp(18), 0);
        root.addView(title);

        TextView subtitle = new TextView(this);
        subtitle.setText("Chạm vào bài hát để nghe • Chỉ cần vuốt lên / xuống");
        subtitle.setTextColor(MUTED);
        subtitle.setTextSize(15);
        subtitle.setPadding(dp(18), dp(8), dp(18), dp(14));
        root.addView(subtitle);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setFillViewport(true);
        scrollView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        LinearLayout list = new LinearLayout(this);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(dp(14), dp(4), dp(14), dp(18));
        scrollView.addView(list);

        LinearLayout.LayoutParams scrollParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        0,
                        1f
                );
        root.addView(scrollView, scrollParams);

        buildPlayerBar();
        root.addView(playerBar);

        setContentView(root);

        loadSongs();

        if (songs.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("Chưa có bài hát.\nHãy chạy GitHub Action để tải các bài được phép sử dụng và tạo APK.");
            empty.setTextColor(MUTED);
            empty.setTextSize(18);
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(dp(24), dp(64), dp(24), dp(64));
            list.addView(empty);
        } else {
            for (int i = 0; i < songs.size(); i++) {
                list.addView(createSongCard(i, songs.get(i)));
            }
        }
    }

    private void buildPlayerBar() {
        playerBar = new LinearLayout(this);
        playerBar.setOrientation(LinearLayout.HORIZONTAL);
        playerBar.setGravity(Gravity.CENTER_VERTICAL);
        playerBar.setPadding(dp(16), dp(12), dp(16), dp(12));
        playerBar.setBackground(rounded(CARD, 0, 0));
        playerBar.setVisibility(View.GONE);

        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);

        nowTitle = new TextView(this);
        nowTitle.setTextColor(TEXT);
        nowTitle.setTextSize(17);
        nowTitle.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        nowTitle.setMaxLines(1);

        nowArtist = new TextView(this);
        nowArtist.setTextColor(MUTED);
        nowArtist.setTextSize(13);
        nowArtist.setMaxLines(1);
        nowArtist.setPadding(0, dp(3), 0, 0);

        info.addView(nowTitle);
        info.addView(nowArtist);

        LinearLayout.LayoutParams infoParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        playerBar.addView(info, infoParams);

        playerToggle = new Button(this);
        playerToggle.setText("⏸");
        playerToggle.setTextSize(19);
        playerToggle.setTextColor(Color.WHITE);
        playerToggle.setBackgroundTintList(ColorStateList.valueOf(ACCENT));
        playerToggle.setMinWidth(dp(56));
        playerToggle.setMinHeight(dp(56));
        playerToggle.setOnClickListener(v -> toggleCurrent());
        playerBar.addView(playerToggle);
    }

    private View createSongCard(int index, Song song) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setPadding(dp(16), dp(16), dp(12), dp(16));
        row.setBackground(rounded(CARD, dp(24), BORDER));
        row.setClickable(true);
        row.setFocusable(true);

        LinearLayout.LayoutParams rowParams =
                new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
        rowParams.setMargins(0, dp(7), 0, dp(7));
        row.setLayoutParams(rowParams);

        TextView icon = new TextView(this);
        icon.setText(song.emoji);
        icon.setTextSize(30);
        icon.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(dp(64), dp(64));
        row.addView(icon, iconParams);

        LinearLayout textBox = new LinearLayout(this);
        textBox.setOrientation(LinearLayout.VERTICAL);
        textBox.setPadding(dp(12), 0, dp(10), 0);

        TextView name = new TextView(this);
        name.setText(song.title);
        name.setTextColor(TEXT);
        name.setTextSize(21);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        name.setMaxLines(2);

        TextView artist = new TextView(this);
        artist.setText(song.artist == null || song.artist.isEmpty() ? "Nhạc cho bé" : song.artist);
        artist.setTextColor(MUTED);
        artist.setTextSize(14);
        artist.setPadding(0, dp(6), 0, 0);
        artist.setMaxLines(1);

        textBox.addView(name);
        textBox.addView(artist);

        LinearLayout.LayoutParams textParams =
                new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        row.addView(textBox, textParams);

        Button play = new Button(this);
        play.setText("▶");
        play.setTextSize(18);
        play.setTextColor(Color.WHITE);
        play.setBackgroundTintList(ColorStateList.valueOf(ACCENT));
        play.setMinWidth(dp(58));
        play.setMinHeight(dp(58));
        play.setOnClickListener(v -> playOrToggle(index));
        row.addView(play);

        playButtons.add(play);
        row.setOnClickListener(v -> playOrToggle(index));

        return row;
    }

    private void playOrToggle(int index) {
        if (index == currentIndex && mediaPlayer != null && isPrepared) {
            toggleCurrent();
            return;
        }

        releasePlayer();

        try {
            Song song = songs.get(index);
            AssetFileDescriptor afd = getAssets().openFd(song.file);

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                            .setUsage(AudioAttributes.USAGE_MEDIA)
                            .build()
            );
            mediaPlayer.setDataSource(
                    afd.getFileDescriptor(),
                    afd.getStartOffset(),
                    afd.getLength()
            );
            afd.close();

            mediaPlayer.setVolume(0.82f, 0.82f);
            mediaPlayer.prepare();
            isPrepared = true;
            currentIndex = index;

            mediaPlayer.setOnCompletionListener(mp -> {
                int next = currentIndex + 1;
                if (next < songs.size()) {
                    playOrToggle(next);
                } else {
                    resetButtons();
                    playerToggle.setText("▶");
                }
            });

            mediaPlayer.start();
            updateNowPlaying(song, true);
            resetButtons();
            playButtons.get(index).setText("⏸");

        } catch (Exception error) {
            releasePlayer();
            nowTitle.setText("Không phát được bài hát");
            nowArtist.setText(error.getMessage() == null ? "Lỗi audio" : error.getMessage());
            playerBar.setVisibility(View.VISIBLE);
        }
    }

    private void toggleCurrent() {
        if (mediaPlayer == null || !isPrepared || currentIndex < 0) {
            return;
        }

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            playerToggle.setText("▶");
            if (currentIndex < playButtons.size()) {
                playButtons.get(currentIndex).setText("▶");
            }
        } else {
            mediaPlayer.start();
            playerToggle.setText("⏸");
            if (currentIndex < playButtons.size()) {
                playButtons.get(currentIndex).setText("⏸");
            }
        }
    }

    private void updateNowPlaying(Song song, boolean playing) {
        nowTitle.setText(song.title);
        nowArtist.setText(song.artist == null ? "" : song.artist);
        playerToggle.setText(playing ? "⏸" : "▶");
        playerBar.setVisibility(View.VISIBLE);
    }

    private void resetButtons() {
        for (Button button : playButtons) {
            button.setText("▶");
        }
    }

    private void loadSongs() {
        songs.clear();

        try {
            String json = readAsset("songs.json");
            JSONArray array = new JSONArray(json);

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);

                String title = item.optString("title", "Bài hát");
                String artist = item.optString("artist", "");
                String file = item.getString("file");
                String emoji = item.optString("emoji", "🎵");

                songs.add(new Song(title, artist, file, emoji));
            }
        } catch (Exception ignored) {
        }
    }

    private String readAsset(String path) throws Exception {
        try (InputStream input = getAssets().open(path);
             ByteArrayOutputStream output = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[4096];
            int read;

            while ((read = input.read(buffer)) != -1) {
                output.write(buffer, 0, read);
            }

            return output.toString(StandardCharsets.UTF_8.name());
        }
    }

    private GradientDrawable rounded(int fillColor, int radiusPx, int strokeColor) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(fillColor);
        drawable.setCornerRadius(radiusPx);
        if (strokeColor != 0) {
            drawable.setStroke(dp(1), strokeColor);
        }
        return drawable;
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void releasePlayer() {
        isPrepared = false;

        if (mediaPlayer != null) {
            try {
                mediaPlayer.stop();
            } catch (Exception ignored) {
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }

        resetButtons();
    }

    @Override
    protected void onDestroy() {
        releasePlayer();
        super.onDestroy();
    }

    private static class Song {
        final String title;
        final String artist;
        final String file;
        final String emoji;

        Song(String title, String artist, String file, String emoji) {
            this.title = title;
            this.artist = artist;
            this.file = file;
            this.emoji = emoji;
        }
    }
}
