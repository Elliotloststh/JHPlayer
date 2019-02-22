package MusicPlayer;

import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.AudioDeviceBase;

import javax.sound.sampled.*;

public class myAudioDevice extends AudioDeviceBase {
    private SourceDataLine source = null;
    private AudioFormat fmt = null;
    private byte[] byteBuf = new byte[4096];
    int position = 0;

    public myAudioDevice() {
    }

    protected void setAudioFormat(AudioFormat fmt0) {
        this.fmt = fmt0;
    }

    protected AudioFormat getAudioFormat() {
        if (this.fmt == null) {
            Decoder decoder = this.getDecoder();
            this.fmt = new AudioFormat((float)decoder.getOutputFrequency(), 16, decoder.getOutputChannels(), true, false);
        }

        return this.fmt;
    }

    protected DataLine.Info getSourceLineInfo() {
        AudioFormat fmt = this.getAudioFormat();
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, fmt);
        return info;
    }

    public void open(AudioFormat fmt) throws JavaLayerException {
        if (!this.isOpen()) {
            this.setAudioFormat(fmt);
            this.openImpl();
            this.setOpen(true);
        }

    }

    protected void openImpl() throws JavaLayerException {
    }

    protected void createSource() throws JavaLayerException {
        Object t = null;

        try {
            Line line = AudioSystem.getLine(this.getSourceLineInfo());
            if (line instanceof SourceDataLine) {
                this.source = (SourceDataLine)line;
                this.source.open(this.fmt);
                this.source.start();
            }
        } catch (RuntimeException var3) {
            t = var3;
        } catch (LinkageError var4) {
            t = var4;
        } catch (LineUnavailableException var5) {
            t = var5;
        }

        if (this.source == null) {
            throw new JavaLayerException("cannot obtain source audio line", (Throwable)t);
        }
    }

    public int millisecondsToBytes(AudioFormat fmt, int time) {
        return (int)((double)((float)time * fmt.getSampleRate() * (float)fmt.getChannels() * (float)fmt.getSampleSizeInBits()) / 8000.0D);
    }

    protected void closeImpl() {
        if (this.source != null) {
            this.source.close();
        }

    }

    protected void writeImpl(short[] samples, int offs, int len) throws JavaLayerException {
        if (this.source == null) {
            this.createSource();
        }

        byte[] b = this.toByteArray(samples, offs, len);
        this.source.write(b, 0, len * 2);
    }

    protected byte[] getByteArray(int length) {
        if (this.byteBuf.length < length) {
            this.byteBuf = new byte[length + 1024];
        }

        return this.byteBuf;
    }

    protected byte[] toByteArray(short[] samples, int offs, int len) {
        byte[] b = this.getByteArray(len * 2);

        short s;
        for(int var5 = 0; len-- > 0; b[var5++] = (byte)(s >>> 8)) {
            s = samples[offs++];
            b[var5++] = (byte)s;
        }

        return b;
    }

    protected void flushImpl() {
        if (this.source != null) {
            this.source.drain();
        }

    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int p) {
        position = p;
    }

    public void test() throws JavaLayerException {
        try {
            this.open(new AudioFormat(22050.0F, 16, 1, true, false));
            short[] data = new short[2205];
            this.write(data, 0, data.length);
            this.flush();
            this.close();
        } catch (RuntimeException var2) {
            throw new JavaLayerException("Device test failed: " + var2);
        }
    }

    public void pause() {
        if (this.source != null) {
            this.source.stop();
        }
    }

    public void recover() {
        if (this.source != null) {
            this.source.start();
        }
    }
}
