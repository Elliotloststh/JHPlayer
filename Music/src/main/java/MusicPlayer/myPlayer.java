package MusicPlayer;

import javazoom.jl.decoder.*;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
import javazoom.jl.player.advanced.PlaybackEvent;
import javazoom.jl.player.advanced.PlaybackListener;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class myPlayer {
    private enum PlayerState {
        PAUSE, PLAY, FINISHED
    }

    private static final int BLOCK_SIZE = 20;

    private PlayerState state = PlayerState.PAUSE;
    private int playSpeed = 0;
    private int blockCount = 0;
    private int skipCount = 0;

    private Thread thread;
    boolean closed = false;

    private Bitstream bitstream;
    private final Decoder decoder;
    private myAudioDevice audio;


    private final List<myPlaybackListener> listeners = new ArrayList<myPlaybackListener>();

//    public myPlayer(InputStream in) throws JavaLayerException {
//        this(in, null);
//    }

    public myPlayer(InputStream in, final myAudioDevice audio) throws JavaLayerException {
        this.bitstream = new Bitstream(in);
        this.decoder = new Decoder();
        this.audio = audio;
        this.audio.open(decoder);

        this.thread = new Thread(new Runnable() {
            public void run() {
                while(!PlayerState.FINISHED.equals(state)) {
                    if(PlayerState.PLAY.equals(state)) {
                        try {
                            boolean playFrame = true;

                            if(playSpeed > 0) { // fast forwarding
                                playFrame = (skipCount >= Math.pow(playSpeed, 2) * BLOCK_SIZE);
                                if(playFrame) { // haven't skipped enough frames yet
                                    blockCount++;
                                    if(blockCount == BLOCK_SIZE) {
                                        skipCount = 0;
                                        blockCount = 0;
                                    }
                                } else {
                                    skipCount++;
                                }
                            }

                            if(readFrame(playFrame) == -1) {
                                listeners.get(0).playbackFinished();
                                stopthread();
                            }
                        } catch(JavaLayerException e) {
                            throw new RuntimeException(e);
                        }
                    } else if(PlayerState.PAUSE.equals(state)) {
                        try {
                            synchronized(thread) {
                                audio.pause();
                                thread.wait();
                            }
                        } catch(InterruptedException ignored) {
                            audio.recover();
                        }
                    }
                }
            }
        });
        this.thread.start();
    }

    public void stopthread() {
        state = PlayerState.FINISHED;
    }


    public void addListener(myPlaybackListener listener) {
        listeners.add(0,listener);
    }

    public void play() {
        state = PlayerState.PLAY;
        synchronized(thread) {
            thread.interrupt();
        }
    }

    public void pause() {
        state = PlayerState.PAUSE;
    }

    public boolean isPlaying() {
        return PlayerState.PLAY.equals(state);
    }

    public synchronized void close() {
        if(!closed) {
            closed = true;
            state = PlayerState.FINISHED;
            synchronized(thread) {
                try {
                    thread.interrupt();
                    thread.join(2000);
                } catch(InterruptedException ignored) {
                }
            }
            audio.flush();
            audio.close();
            try {
                bitstream.close();
            } catch(BitstreamException ignored) {
            }
        }
    }

    public int getPosition() {
        return audio.getPosition();
    }

    public float readFrame(boolean play) throws JavaLayerException {
        Header header = bitstream.readFrame();

        if(header == null) {
            return -1; // playback finished
        } else {
            if(play) {
                SampleBuffer output = (SampleBuffer) decoder.decodeFrame(header, bitstream);

                synchronized(this) {
                    audio.write(output.getBuffer(), 0, output.getBufferLength());
                }
            }

            bitstream.closeFrame();

            return header.ms_per_frame();
        }
    }
}