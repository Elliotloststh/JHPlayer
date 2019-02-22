package MusicPlayer;

import GUI.MainWindow;
import com.sun.tools.javac.Main;
import javazoom.jl.decoder.JavaLayerException;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;

class music {
    boolean downloaded;
    String src;
    music(String s, boolean b) {
        downloaded = b;
        src = s;
    }
}

public class playList {
    private final myAudioDevice audioDevice;
    private List<music> list;
    private int cursor;
    private myPlayer player = null;
    private MainWindow window;


    public playList(myAudioDevice audioDevice, MainWindow w) {
        this.audioDevice = audioDevice;
        this.list = new LinkedList<music>();
        window = w;
        this.cursor = -1;
    }

    public synchronized boolean next() throws FileNotFoundException, JavaLayerException {
        if(hasNext()) {
            if(player != null) {
                player.stopthread();
            }
            window.nextSong();
            gotoMusic(list.get(++cursor));
            return true;
        } else {
            if(player != null) {
                player.stopthread();
            }
            cursor = -1;
            window.nextSong();
            gotoMusic(list.get(++cursor));
            return false;
        }
    }

    public synchronized boolean previous() throws JavaLayerException, FileNotFoundException {
        if(hasPrevious()) {
            if(player != null) {
                player.stopthread();
            }
            window.previousSong();
            gotoMusic(list.get(--cursor));
            return true;
        } else {
            if(player != null) {
                player.stopthread();
            }
            cursor = list.size();
            window.previousSong();
            gotoMusic(list.get(--cursor));
            return false;
        }
    }


    protected void gotoMusic(music m) throws FileNotFoundException, JavaLayerException {
        if(m.downloaded) {
            File file = new File(m.src);
            player = new myPlayer(new FileInputStream(file),audioDevice);
        } else {
            try {
                player = new myPlayer(new BufferedInputStream(new URL(m.src).openStream()),audioDevice);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        player.addListener(new myPlaybackListener() {
            public boolean playbackFinished() {
                try {
                    return next();
                } catch(FileNotFoundException e) {
                    e.printStackTrace();
                    return false;
                } catch(JavaLayerException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        });
        player.play();
        window.counterStart();

    }

    public void play() throws JavaLayerException, FileNotFoundException {
        if(player != null) {
            player.play();
        } else {
            next();
        }
    }

    public void pause() {
        if(player != null) {
            player.pause();
        }
    }


    public void addSong(String src, boolean downloaded) {
        music m = new music(src, downloaded);
        list.add(m);
    }

    public void adjustSong(String src, int index) {
        list.get(index).src = src;
    }

    public void adjustSong(boolean downloaded, int index) {
        list.get(index).downloaded = downloaded;
    }

    public void remove(int index) {
        list.remove(index);
        if(index <= cursor)
        {
            cursor--;
        }
    }

    public void setCursor(int _c) {
        cursor = _c;
    }

    public void clearList() {
        if(player!=null) {
            player.stopthread();
        }
        list.clear();
        cursor = -1;
    }

    public boolean hasNext() {
        return cursor < list.size()-1;
    }

    public boolean hasPrevious() {
        return cursor > 0;
    }
}

