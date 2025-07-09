package com.example.eduquizz.data_save

import android.content.Context
import android.media.MediaPlayer
import android.media.SoundPool
import com.example.eduquizz.R

object AudioManager {
    private var bgmPlayer: MediaPlayer? = null
    private var soundPool: SoundPool? = null
    private var clickSoundId: Int = 0
    private var sfxVolume: Float = 1f
    private var bgmVolume: Float = 1f 

    fun init(context: Context) {
        // Nhạc nền
        if (bgmPlayer == null) {
            bgmPlayer = MediaPlayer.create(context, R.raw.bgsound) 
            bgmPlayer?.isLooping = true
            bgmPlayer?.setVolume(bgmVolume, bgmVolume)
        }
        // SFX
        if (soundPool == null) {
            soundPool = SoundPool.Builder().setMaxStreams(2).build()
            clickSoundId = soundPool!!.load(context, R.raw.click, 1)
        }
    }

    fun playBgm() {
        bgmPlayer?.start()
    }

    fun pauseBgm() {
        bgmPlayer?.pause()
    }

    fun setBgmEnabled(enabled: Boolean) {
        if (enabled) playBgm() else pauseBgm()
    }

    fun setBgmVolume(volume: Float) { 
        bgmVolume = volume
        bgmPlayer?.setVolume(volume, volume)
    }
    fun playClickSfx() {
        soundPool?.play(clickSoundId, sfxVolume, sfxVolume, 1, 0, 1f)
    }

    fun setSfxVolume(volume: Float) {
        sfxVolume = volume
    }

    fun release() {
        bgmPlayer?.release()
        bgmPlayer = null
        soundPool?.release()
        soundPool = null
    }
} 