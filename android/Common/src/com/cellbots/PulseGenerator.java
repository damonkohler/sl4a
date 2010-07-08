/*
 * Robot control console. Copyright (C) 2010 Darrell Taylor & Eric Hokanson
 * 
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.cellbots;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.util.Log;

public class PulseGenerator implements Runnable {

  // 44100 native on g1
  private int sampleRate;

  public int MIN_PULSE_WIDTH;

  public int MAX_PULSE_WIDTH;

  private int lPulseWidth;

  private int rPulseWidth;

  private int pulseInterval;

  private int bufferPulses = 2;

  private int volume = 30000;

  private int modulation = 200;

  private boolean playing = false;

  private boolean bufferChanged = false;

  private AudioTrack noiseAudioTrack;

  private int bufferlength; // 4800

  private boolean inverted = true;

  private short[] audioBuffer;

  private short[] leftChannelBuffer;

  private short[] rightChannelBuffer;

  public PulseGenerator() {
    sampleRate = AudioTrack.getNativeOutputSampleRate(AudioManager.STREAM_MUSIC);

    MIN_PULSE_WIDTH = sampleRate / 1200;

    MAX_PULSE_WIDTH = sampleRate / 456;

    lPulseWidth = (MIN_PULSE_WIDTH + MAX_PULSE_WIDTH) / 2;

    rPulseWidth = (MIN_PULSE_WIDTH + MAX_PULSE_WIDTH) / 2;

    pulseInterval = sampleRate / 50;

    bufferlength =
        AudioTrack.getMinBufferSize(sampleRate, AudioFormat.CHANNEL_CONFIGURATION_STEREO,
            AudioFormat.ENCODING_PCM_16BIT);

    noiseAudioTrack =
        new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
            AudioFormat.CHANNEL_CONFIGURATION_STEREO, AudioFormat.ENCODING_PCM_16BIT, bufferlength,
            AudioTrack.MODE_STREAM);

    sampleRate = noiseAudioTrack.getSampleRate();

    Log.i("Noise Setup", "BufferLength = " + Integer.toString(bufferlength));
    Log.i("Noise Setup", "Sample Rate = " + Integer.toString(sampleRate));

    audioBuffer = new short[bufferlength];
    leftChannelBuffer = new short[bufferlength / 2];
    rightChannelBuffer = new short[bufferlength / 2];

    noiseAudioTrack.play();
  }

  private void generatePCM(int pulseWidth, int pulseInterval, int volume, int modulation,
      short buffer[], int bufferLength) {
    int inverter = 1;

    if (inverted) {
      inverter = -1;
    }

    int i = 0;
    int j = 0;

    while (i < bufferLength) {
      j = 0;
      while (j < pulseWidth)// && i < bufferLength)
      {
        // we have to modulate the signal a bit because the sound card freaks
        // out if it goes dc
        buffer[i] = (short) ((volume * inverter) + ((i % 2) * modulation));
        i++;
        j++;
      }

      while (j < pulseInterval)// && i < bufferLength)
      {
        buffer[i] = (short) ((-volume * inverter) + ((i % 2) * modulation));
        i++;
        j++;
      }

    }

    bufferChanged = true;
  }

  public void run() {
    generatePCM(lPulseWidth, pulseInterval, volume, modulation, leftChannelBuffer, pulseInterval
        * bufferPulses);
    generatePCM(rPulseWidth, pulseInterval, volume, modulation, rightChannelBuffer, pulseInterval
        * bufferPulses);
    while (true) {
      int bufferlength = pulseInterval * bufferPulses * 2;
      if (playing) {
        // Log.i("Pulse Generator", "extraSamples" + Integer.toString(extraLeftPulses));
        for (int i = 0; i < bufferlength && bufferChanged; i += 2) {
          audioBuffer[i] = leftChannelBuffer[i / 2];
          audioBuffer[i + 1] = rightChannelBuffer[i / 2];
        }

      } else {
        for (int i = 0; i < bufferlength; i++) {
          audioBuffer[i] = (short) (0);
        }
      }

      noiseAudioTrack.write(audioBuffer, 0, bufferlength);
    }
  }

  public void stop() {
    playing = false;
    noiseAudioTrack.stop();
    noiseAudioTrack.release();
  }

  public void togglePlayback() {
    playing = !playing;
  }

  public void toggleInverted() {
    inverted = !inverted;
  }

  public boolean isPlaying() {
    return playing;
  }

  public void setLeftPulsePercent(int percent) {
    lPulseWidth = MIN_PULSE_WIDTH + ((percent * (MAX_PULSE_WIDTH - MIN_PULSE_WIDTH)) / 100);
    generatePCM(lPulseWidth, pulseInterval, volume, modulation, leftChannelBuffer, bufferPulses
        * pulseInterval);
  }

  public int getLeftPulsePercent() {
    return ((lPulseWidth - MIN_PULSE_WIDTH) / (MAX_PULSE_WIDTH - MIN_PULSE_WIDTH)) * 100;
  }

  public float getLeftPulseMs() {
    return ((float) lPulseWidth / sampleRate) * 1000;
  }

  public int getLeftPulseSamples() {
    return lPulseWidth;
  }

  public void setRightPulsePercent(int percent) {
    // mirror the right servo
    // percent = 100 - percent;

    rPulseWidth = MIN_PULSE_WIDTH + ((percent * (MAX_PULSE_WIDTH - MIN_PULSE_WIDTH)) / 100);

    generatePCM(rPulseWidth, pulseInterval, volume, modulation, rightChannelBuffer, bufferPulses
        * pulseInterval);
  }

  public float getRightPulseMs() {
    return ((float) rPulseWidth / sampleRate) * 1000;
  }

  public int getRightPulsePercent() {
    return ((rPulseWidth - MIN_PULSE_WIDTH) / (MAX_PULSE_WIDTH - MIN_PULSE_WIDTH)) * 100;
  }

  public int getRightPulseSamples() {
    return rPulseWidth;
  }

  public float getHz() {
    return (float) sampleRate / pulseInterval;
  }

  public int getHzSamples() {
    return pulseInterval;
  }

  public void setHz(float hz) {
    pulseInterval = (int) (sampleRate / hz);

    generatePCM(lPulseWidth, pulseInterval, volume, modulation, leftChannelBuffer, bufferPulses
        * pulseInterval);
    generatePCM(rPulseWidth, pulseInterval, volume, modulation, rightChannelBuffer, bufferPulses
        * pulseInterval);
  }

  public void setHzPercent(int percent) {
    float hz = 50;

    hz = (float) (hz + (percent - 50.0) / 10.0);

    setHz(hz);
  }

}