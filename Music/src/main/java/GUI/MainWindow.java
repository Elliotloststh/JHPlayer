package GUI;

import Grabber.KugouGrabber;
import Grabber.NeteaseGrabber;
import Grabber.QQGrabber;
import MusicPlayer.myAudioDevice;
import MusicPlayer.playList;
import Utils.SongInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mpatric.mp3agic.ID3v1;
import com.mpatric.mp3agic.ID3v2;
import com.mpatric.mp3agic.Mp3File;
import com.mysql.cj.xdevapi.Table;
import org.apache.commons.lang.StringUtils;
import server.Message;
import server.MyServer;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.MatteBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;

import static Utils.fileOperation.deleteFile;
import static javax.swing.BorderFactory.createEmptyBorder;

class indexLamda {
    public int index;
    public indexLamda(int i) {
        index = i;
    }
    public void setindex(int i) {
        index = i;
    }
}

public class MainWindow {
    InputStream in;
    OutputStream out;
    NeteaseGrabber ng = new NeteaseGrabber();
    QQGrabber qg = new QQGrabber();
    KugouGrabber kg = new KugouGrabber();
    myAudioDevice device = new myAudioDevice();
    playList songList = new playList(device, this);
    LinkedList<SongInfo> playSongs = new LinkedList<>();
    LinkedList<SongInfo> localSongs = new LinkedList<>();
    LinkedList<SongInfo> loveSongs = new LinkedList<>();
    LinkedList<SongInfo> searchSongs = new LinkedList<>();
    MyTable tableLocal = new MyTable();
    MyTable tableLove = new MyTable();
    MyTable tableSearch = new MyTable();
    JTextField search = new JTextField();
    SongInfo currentSong;
    boolean playing = false;
    int songType; //0-downloaded, 1-favorite, 2-search
    int cursor = -1;
    JPanel panel;
    JLabel labelFace;
    JLabel title;
    JLabel singer;
    JButton favorite = new JButton();
    JButton download = new JButton();
    JButton previous = new JButton();
    JButton pause = new JButton();
    JButton next = new JButton();
    JLabel currentTime = new JLabel();
    JLabel totalTime = new JLabel();
    String account = "ccyy";
    counterThread thread = null;
    boolean downloaded;
    ImageIcon picFavorite;
    ImageIcon picUnFavorite;
    ImageIcon picDownload;
    ImageIcon picUnDownload;
    ImageIcon picPause;
    ImageIcon picPlay;


    public static void main(String[] args) {
        MainWindow gui = new MainWindow();
        gui.init();
        gui.login();
    }

    public void counterStart() {
        thread = new counterThread();
        thread.start();
    }

    public void nextSong() {
        if(thread!=null) {
            thread.stopThread();
        }
        if(cursor == playSongs.size()-1) {
            cursor = -1;
        }
        cursor++;
        currentSong = playSongs.get(cursor);

        title.setText(currentSong.getTitle());
        singer.setText(currentSong.getSinger());
        currentTime.setText("00:00");
        long time = currentSong.getLength();
        totalTime.setText(String.format("%02d", time/60)+":"+String.format("%02d", time%60));
        if(songType == 0) {

            downloaded = true;
            download.setIcon(picDownload);

            if(currentSong.getStarred()) {
                favorite.setIcon(picFavorite);
            } else {
                favorite.setIcon(picUnFavorite);
            }

        } else {
            String basePath = "../download/Media/";
            String src;
            if(currentSong.getSrc() == 0) {
                src = "netease/";
            } else if(currentSong.getSrc() == 1) {
                src = "qqmusic/";
            } else {
                src = "kugou/";
            }
            File f = new File(basePath+src+currentSong.getId()+".mp3");
            if(f.exists()) {
                currentSong.setLocalurl(basePath+src+currentSong.getId()+".mp3");
                downloaded = true;
                download.setIcon(picDownload);
            } else {
                if(currentSong.getSrc() == 0) {
                    try {
                        songList.adjustSong(ng.songUrlGet(currentSong.getId()), cursor);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                downloaded = false;
                download.setIcon(picUnDownload);
            }

            if(currentSong.getStarred()) {
                favorite.setIcon(picFavorite);
            } else {
                favorite.setIcon(picUnFavorite);
            }
        }
        currentSong.print();
    }

    public void previousSong() {
        if(thread!=null) {
            thread.stopThread();
        }
        if(cursor == 0) {
            cursor = playSongs.size();
        }
        cursor--;
        currentSong = playSongs.get(cursor);

        title.setText(currentSong.getTitle());
        singer.setText(currentSong.getSinger());
        currentTime.setText("00:00");
        long time = currentSong.getLength();
        totalTime.setText(String.format("%02d", time/60)+":"+String.format("%02d", time%60));
        if(songType == 0) {

            downloaded = true;
            download.setIcon(picDownload);

            if(currentSong.getStarred()) {
                favorite.setIcon(picFavorite);
            } else {
                favorite.setIcon(picUnFavorite);
            }

        } else {
            String basePath = "../download/Media/";
            String src;
            if(currentSong.getSrc() == 0) {
                src = "netease/";
            } else if(currentSong.getSrc() == 1) {
                src = "qqmusic/";
            } else {
                src = "kugou/";
            }
            File f = new File(basePath+src+currentSong.getId()+".mp3");
            if(f.exists()) {
                currentSong.setLocalurl(basePath+src+currentSong.getId()+".mp3");
                downloaded = true;
                download.setIcon(picDownload);
            } else {
                if(currentSong.getSrc() == 0) {
                    try {
                        songList.adjustSong(ng.songUrlGet(currentSong.getId()), cursor);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                downloaded = false;
                download.setIcon(picUnDownload);
            }

            if(currentSong.getStarred()) {
                favorite.setIcon(picFavorite);
            } else {
                favorite.setIcon(picUnFavorite);
            }
        }
        currentSong.print();
    }

    public void PlayerWindow() {
        JFrame frame = new JFrame();
        frame.setSize(800, 600);
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        // 添加面板
        frame.add(panel);
        placeComponentsPlayer(panel, frame);

        frame.setLocationRelativeTo(null);
        frame.setVisible(true);

    }

    public void placeComponentsPlayer(JPanel panel, JFrame frame) {
        panel.setLayout(new BorderLayout(1,0));

        JPanel panelOp = new JPanel();
        panelOp.setPreferredSize(new Dimension(130, 500));
        panelOp.setBackground(Color.WHITE);

        CardLayout cl = new CardLayout();
        JPanel panelWork = new JPanel(cl);
        JPanel panelDefault = new JPanel();
        panelDefault.setBackground(Color.WHITE);
        JLabel bg = new JLabel();
        ImageIcon picDefault = new ImageIcon("src/main/resources/bg.jpg");
        picDefault.setImage(picDefault.getImage().getScaledInstance(670,500,Image.SCALE_SMOOTH));
        bg.setIcon(picDefault);
        panelDefault.add(bg);

        JPanel panelLocal = new JPanel();
        panelLocal.setBackground(Color.WHITE);
        JPanel panelLove = new JPanel();
        panelLove.setBackground(Color.WHITE);
        JPanel panelSearch = new JPanel();
        panelSearch.setBackground(Color.WHITE);
        panelWork.add(panelDefault, "default");
        panelWork.add(panelLocal,"localMusic");
        placeLocalMusic(panelLocal);
        panelWork.add(panelLove, "loveMusic");
        placeLoveMusic(panelLove);
        panelWork.add(panelSearch, "search");
        placeSearchMusic(panelSearch);
        panelWork.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panelWork.requestFocus();
            }
        });

        JPanel panelPlayer = new JPanel();
        panelPlayer.setBackground(Color.decode("#FFF3E7"));
        panelPlayer.setPreferredSize(new Dimension(800, 100));
        panelPlayer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panelWork.requestFocus();
            }
        });
        placePlayer(panelPlayer);

        panel.add(panelOp, BorderLayout.WEST);
        panel.add(panelPlayer, BorderLayout.SOUTH);
        panel.add(panelWork, BorderLayout.CENTER);



        //panelOp 左侧操作区
        JPanel panelEmpty = new JPanel();
        panelEmpty.setBackground(Color.WHITE);
        panelEmpty.setPreferredSize(new Dimension(130, 50));


        JPanel panelLogo = new JPanel();
        panelLogo.setBackground(Color.WHITE);
        panelLogo.setPreferredSize(new Dimension(130, 110));
        JList<String> list = new JList<>();
        final indexLamda indexOp = new indexLamda(-1);

        ImageIcon img = new ImageIcon("src/main/resources/未选中.png");
        ImageIcon img2 = new ImageIcon("src/main/resources/选中.png");
        img.setImage(img.getImage().getScaledInstance(60,60,Image.SCALE_SMOOTH));
        img2.setImage(img2.getImage().getScaledInstance(60,60,Image.SCALE_SMOOTH));
        JLabel labelLogo = new JLabel("", img2, JLabel.CENTER);
        labelLogo.addMouseListener(new MouseAdapter(){
            public void mouseClicked(MouseEvent e){
                //TODO
                labelLogo.setIcon(img2);
                list.setSelectedIndices(new int[] {});
                labelLogo.requestFocus();
                cl.show(panelWork, "default");
            }
        });
        panelLogo.add(labelLogo);

        JLabel tip1 = new JLabel("我的音乐");
        tip1.setForeground(Color.decode("#9FB6CD"));

        list.setListData(new String[]{"本地音乐", "我喜欢"});
        list.setPreferredSize(new Dimension(130, 200));
        list.setFixedCellHeight(80);
        list.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        DefaultListCellRenderer renderer = new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list,
                                                          Object value, int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                JLabel listCellRendererComponent = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected,cellHasFocus);
                listCellRendererComponent.setBorder(BorderFactory.createEmptyBorder());
                return listCellRendererComponent;
            }
        };
        renderer.setHorizontalAlignment(SwingConstants.CENTER);
        list.setCellRenderer(renderer);
        list.setBackground(new Color(255,255,255,0));
        list.setOpaque(false);
        list.setSelectionBackground(Color.decode("#EDEDED"));       //选中背景颜色
        list.setForeground(Color.black);
        list.setSelectionForeground(Color.decode("#EE3B3B"));       //选中字颜色

        list.addListSelectionListener(e -> {
            if(list.isSelectionEmpty()) {
                return;
            }
            boolean adjust = e.getValueIsAdjusting();
            if(!adjust) {
                // 获取被选中的选项索引
                int index = list.getSelectedIndex();
                indexOp.setindex(index);
                // 获取选项数据的 ListModel
                ListModel<String> listModel = list.getModel();
                // 输出选中的选项
                labelLogo.setIcon(img);
                if(index==0) {
                    cl.show(panelWork, "localMusic");
                    freshLocalSongs();
                } else if(index==1) {
                    cl.show(panelWork, "loveMusic");
                    freshLoveSongs();
                }
            }

        });

        search.setBackground(new Color(255, 255, 255));
        search.setPreferredSize(new Dimension(129, 28));
        MatteBorder border = new MatteBorder(0, 0, 2, 0, new Color(192, 192,
                192));
        search.setBorder(border);
        search.setText("搜索");
        search.setBackground(Color.WHITE);
        search.setForeground(Color.decode("#838B8B"));
        search.addFocusListener(new FocusListener() {
            @Override
            public void focusLost(FocusEvent e) {//失去焦点时
                if(search.getText().equals("")) {
                    search.setText("搜索");
                }
                search.setBackground(Color.WHITE);
                search.setForeground(Color.decode("#838B8B"));
            }

            @Override
            public void focusGained(FocusEvent e) {//获得焦点时
                if(search.getText().equals("搜索")) {
                    search.setText("");
                }
                search.setBackground(Color.decode("#EDEDED"));
                search.setForeground(Color.BLACK);

            }
        });
        search.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getSource()==search && !search.getText().equals(""))
                {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER) //判断按下的键是否是回车键
                    {
                        cl.show(panelWork, "search");
                        freshSearchSongs();
                    }
                }
            }
        });

        panelOp.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panel.requestFocus();
            }
        });
        panelOp.add(panelEmpty);
        panelOp.add(panelLogo);
        panelOp.add(tip1);
        panelOp.add(list);
        panelOp.add(search);
    }

    public void placePlayer(JPanel panelPlayer) {
        panel = new JPanel();
        panelPlayer.setBackground(Color.decode("#FFF3E7"));
        panelPlayer.setPreferredSize(new Dimension(800, 100));
        panelPlayer.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                panel.requestFocus();
            }
        });
        panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS ));
        panel.setBackground(Color.decode("#FFF3E7"));
        panel.setPreferredSize(new Dimension(800, 100));
        JPanel panelPic = new JPanel();
        panelPic.setLayout(new BoxLayout(panelPic, BoxLayout.X_AXIS ));
        panelPic.setPreferredSize(new Dimension(100, 100));
        panelPic.setBackground(Color.decode("#FFF3E7"));
        panelPic.setBorder(createEmptyBorder(0, 20, 0, 20));
        ImageIcon pic = new ImageIcon("src/main/resources/默认.png");
        pic.setImage(pic.getImage().getScaledInstance(70,70,Image.SCALE_SMOOTH));
        labelFace = new JLabel();
        labelFace.setIcon(pic);
        panelPic.add(labelFace);

        JPanel panelInfo = new JPanel();
        panelInfo.setLayout(new BoxLayout(panelInfo, BoxLayout.Y_AXIS ));
        panelInfo.setBorder(createEmptyBorder(0, 0, 0, 0));
        panelInfo.setBackground(Color.decode("#FFF3E7"));
        panelInfo.setPreferredSize(new Dimension(160, 100));
        title = new JLabel("音乐标题");
        singer = new JLabel("歌手");
        panelInfo.add(title);
        panelInfo.add(Box.createVerticalStrut(10));
        panelInfo.add(singer);

        JPanel panelButton = new JPanel();

        panelButton.setLayout(new BoxLayout(panelButton, BoxLayout.X_AXIS ));

//        panelButton.setBorder(createEmptyBorder(0, 20, 0, 20));
        panelButton.setPreferredSize(new Dimension(530, 100));
        panelButton.setBackground(Color.decode("#FFF3E7"));

        favorite.setBorder(createEmptyBorder());
        favorite.setIcon(picUnFavorite);
        favorite.addActionListener(e -> {
            if(currentSong.getStarred()) {
                JsonObject o = new JsonObject();
                JsonObject d = new JsonObject();
                o.addProperty("type", 4);
                d.addProperty("account", account);
                d.addProperty("songId", currentSong.getId());
                d.addProperty("songSrc", currentSong.getSrc());
                o.add("data", d);
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                    writer.write(o.toString() + "\n");
                    writer.flush();
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = bufferedReader.readLine();
                    Message msg = new Message(line);
                    if(msg.body().get("success").getAsBoolean()) {
                        favorite.setIcon(picUnFavorite);
                        freshLoveSongs();
                        currentSong.setStarred(false);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                JsonObject o = new JsonObject();
                JsonObject d = new JsonObject();
                o.addProperty("type", 5);
                d.addProperty("account", account);
                d.addProperty("songId", currentSong.getId());
                d.addProperty("songSrc", currentSong.getSrc());
                try {
                    if(currentSong.getSrc() == 0) {
                        d.addProperty("songUrl", ng.songUrlGet(currentSong.getId()));
                    } else if(currentSong.getSrc() == 1) {
                        if(songType==0) {
                            d.addProperty("songUrl", qg.songUrlGet(currentSong.getId()));
                        } else {
                            d.addProperty("songUrl", currentSong.getUrl());
                        }
                    } else {
                        if(songType==0) {
                            d.addProperty("songUrl", kg.songUrlGet(currentSong.getId()));
                        } else {
                            d.addProperty("songUrl", currentSong.getUrl());
                        }
                    }
                } catch (Exception e0) {
                    e0.printStackTrace();
                }
                d.addProperty("singer", currentSong.getSinger());
                d.addProperty("album", currentSong.getAlbum());
                d.addProperty("length", currentSong.getLength());
                d.addProperty("title", currentSong.getTitle());
                o.add("data", d);
                try {
                    BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                    writer.write(o.toString() + "\n");
                    writer.flush();
                    InputStreamReader inputStreamReader = new InputStreamReader(in);
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line = bufferedReader.readLine();
                    Message msg = new Message(line);
                    if(msg.body().get("success").getAsBoolean()) {
                        favorite.setIcon(picFavorite);
                        freshLoveSongs();
                        currentSong.setStarred(true);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });

        download.setBorder(createEmptyBorder());
        download.setIcon(picUnDownload);
        download.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(downloaded) {
                    int n = JOptionPane.showConfirmDialog(null, "确认删除吗?", "",JOptionPane.YES_NO_OPTION);
                    if(n==0) {
                        String basePath = "../download/Media/";
                        String src;
                        if(currentSong.getSrc() == 0) {
                            src = "netease/";
                        } else if(currentSong.getSrc() == 1) {
                            src = "qqmusic/";
                        } else {
                            src = "kugou/";
                        }
                        deleteFile(basePath+src+currentSong.getId()+".mp3");

                        if(songType == 0) {
                            songList.remove(cursor);
                            playSongs.remove(cursor);
                            freshLocalSongs();
                            cursor--;
                            try {
                                songList.pause();
                                songList.next();
                                download.setIcon(picUnDownload);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        } else {
                            songList.adjustSong(currentSong.getUrl(), cursor);
                            songList.adjustSong(false, cursor);
                            download.setIcon(picUnDownload);
                        }
                        downloaded = false;
                    }

                } else {
                    try {
                        String basePath = "../download/Media/";
                        String src;
                        if(currentSong.getSrc() == 0) {
                            currentSong.setUrl(ng.songUrlGet(currentSong.getId()));
                            ng.songFileGet(currentSong);
                            src = "netease/";
                        } else if(currentSong.getSrc() == 1) {
                            qg.songFileGet(currentSong);
                            src = "qqmusic/";
                        } else {
                            kg.songFileGet(currentSong);
                            src = "kugou/";
                        }
                        currentSong.setLocalurl(basePath+src+currentSong.getId()+".mp3");
                        songList.adjustSong(currentSong.getLocalurl(), cursor);
                        songList.adjustSong(true, cursor);
                        download.setIcon(picDownload);
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    downloaded = true;
                }
            }
        });

        currentTime.setText("00:00");

        ImageIcon picPre = new ImageIcon("src/main/resources/上一首.png");
        picPre.setImage(picPre.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH));
        previous.setBorder(createEmptyBorder());
        previous.setIcon(picPre);
        previous.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(playSongs.isEmpty()) {
                    return;
                }
                if(!playing) {
                    playing = true;
                    pause.setIcon(picPlay);
                }
                try{
                    songList.previous();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        pause.setBorder(createEmptyBorder());
        pause.setIcon(picPause);
        pause.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(playSongs.isEmpty()) {
                    return;
                }
                if(playing) {
                    playing = false;
                    songList.pause();
                    pause.setIcon(picPause);
                } else {
                    playing = true;
                    pause.setIcon(picPlay);
                    try {
                        songList.play();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }
        });

        ImageIcon picNext = new ImageIcon("src/main/resources/下一首.png");
        picNext.setImage(picNext.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH));
        next.setBorder(createEmptyBorder());
        next.setIcon(picNext);
        next.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(playSongs.isEmpty()) {
                    return;
                }
                if(!playing) {
                    playing = true;
                    pause.setIcon(picPlay);
                }
                try{
                    songList.next();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }
        });

        totalTime.setText("00:00");

        panelButton.add(Box.createHorizontalStrut(50));
        panelButton.add(favorite);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(download);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(currentTime);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(previous);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(pause);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(next);
        panelButton.add(Box.createHorizontalStrut(35));
        panelButton.add(totalTime);

        panel.add(panelPic);
        panel.add(panelInfo);
        panel.add(panelButton);

//        panel.setVisible(false);
        panelPlayer.add(panel);

    }

    public void placeSearchMusic(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(660,500));
        scrollPane.setBorder(createEmptyBorder());
        tableSearch.setPreferredSize(new Dimension(660,480));
        tableSearch.setBorder(createEmptyBorder());
        tableSearch.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tableSearch.clearSelection();
            }
        });
        tableSearch.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    int index = tableSearch.getSelectedRow();
                    if(index < 0)
                        return;
                    songList.clearList();
                    playSongs.clear();
                    String basePath = "../download/Media/";
                    for(SongInfo song:searchSongs) {
                        String src;
                        if(song.getSrc() == 0) {
                            src = "netease/";
                        } else if(song.getSrc() == 1) {
                            src = "qqmusic/";
                        } else {
                            src = "kugou/";
                        }
                        File f = new File(basePath+src+song.getId()+".mp3");
                        if(f.exists()) {
                            songList.addSong(basePath+src+song.getId()+".mp3", true);
                        } else {
                            songList.addSong(song.getUrl(), false);
                        }
                        playSongs.add(song);
                    }
                    try {
                        songType = 2;
                        playing = true;
                        cursor = index-1;
                        songList.setCursor(index-1);
                        songList.next();
                        pause.setIcon(picPlay);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
//        freshSearchSongs();
        scrollPane.setViewportView(tableSearch);
        panel.add(scrollPane);
    }

    public void placeLocalMusic(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(660,500));
        scrollPane.setBorder(createEmptyBorder());
        tableLocal.setPreferredSize(new Dimension(660,480));
        tableLocal.setBorder(createEmptyBorder());
        tableLocal.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tableLocal.clearSelection();
            }
        });
        tableLocal.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    int index = tableLocal.getSelectedRow();
                    if(index < 0)
                        return;

                    songList.clearList();
                    playSongs.clear();
                    String basePath = "../download/Media/";
                    for(SongInfo song:localSongs) {
                        String src;
                        if(song.getSrc() == 0) {
                            src = "netease/";
                        } else if(song.getSrc() == 1) {
                            src = "qqmusic/";
                        } else {
                            src = "kugou/";
                        }
                        songList.addSong(basePath+src+song.getId()+".mp3", true);
                        playSongs.add(song);
                    }
                    try {
                        songType = 0;
                        playing = true;
                        cursor = index-1;
                        songList.setCursor(index-1);
                        songList.next();
                        pause.setIcon(picPlay);

                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        freshLocalSongs();
        scrollPane.setViewportView(tableLocal);
        panel.add(scrollPane);
    }

    public void placeLoveMusic(JPanel panel) {
        JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(660,500));
        scrollPane.setBorder(createEmptyBorder());
        tableLove.setPreferredSize(new Dimension(660,480));
        tableLove.setBorder(createEmptyBorder());
        tableLove.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                tableLove.clearSelection();
            }
        });
        tableLove.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount()==2){
                    int index = tableLove.getSelectedRow();
                    if(index < 0)
                        return;
                    songList.clearList();
                    playSongs.clear();
                    String basePath = "../download/Media/";
                    for(SongInfo song:loveSongs) {
                        String src;
                        if(song.getSrc() == 0) {
                            src = "netease/";
                        } else if(song.getSrc() == 1) {
                            src = "qqmusic/";
                        } else {
                            src = "kugou/";
                        }
                        File f = new File(basePath+src+song.getId()+".mp3");
                        if(f.exists()) {
                            songList.addSong(basePath+src+song.getId()+".mp3", true);
                        } else {
                            songList.addSong(song.getUrl(), false);
                        }
                        playSongs.add(song);
                    }
                    try {
                        songType = 1;
                        playing = true;
                        cursor = index-1;
                        songList.setCursor(index-1);
                        songList.next();
                        pause.setIcon(picPlay);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });
        freshLoveSongs();
        scrollPane.setViewportView(tableLove);
        panel.add(scrollPane);
    }

    private void freshLocalSongs() {
        localSongs.clear();
        String basePath = "../download/Media/";
        addLocalSongs(basePath+"qqmusic/");
        addLocalSongs(basePath+"netease/");
        addLocalSongs(basePath+"kugou/");
        String[] head = new String[]{"","音乐标题", "歌手", "专辑", "时长"};
        DefaultTableModel tableModel=new DefaultTableModel(queryData(head.length),head){
            public boolean isCellEditable(int row, int column)
            {
                return false;
            }
        };
        tableLocal.setModel(tableModel);
        tableLocal.getColumnModel().getColumn(0).setPreferredWidth(40);
        tableLocal.getColumnModel().getColumn(1).setPreferredWidth(280);
        tableLocal.getColumnModel().getColumn(2).setPreferredWidth(130);
        tableLocal.getColumnModel().getColumn(3).setPreferredWidth(130);
        tableLocal.getColumnModel().getColumn(4).setPreferredWidth(60);
    }

    private void addLocalSongs(String path) {
        int src;
        if(path.endsWith("netease/")) {
            src = 0;
        } else if(path.endsWith("qqmusic/")) {
            src = 1;
        } else {
            src = 2;
        }
        File file = new File(path);
        File[] files = file.listFiles();
        for (File it : files) {
            if (!it.isDirectory() && it.getName().endsWith(".mp3")) {
                String songpath = path+it.getName();
                SongInfo song = new SongInfo();
                try {
                    Mp3File mp3file = new Mp3File(songpath);
                    if (mp3file.hasId3v2Tag()) {
                        ID3v2 id3v2Tag = mp3file.getId3v2Tag();
                        song.setSinger(id3v2Tag.getArtist());
                        song.setTitle(id3v2Tag.getTitle());
                        song.setAlbum(id3v2Tag.getAlbum());
                        song.setDownloaded(true);
                        song.setLocalurl(songpath);
                        song.setLength(mp3file.getLengthInSeconds());
                        song.setSrc(src);
                        song.setId((it.getName().substring(0,it.getName().lastIndexOf("."))));
                        JsonObject o = new JsonObject();
                        JsonObject d = new JsonObject();
                        o.addProperty("type", 3);
                        d.addProperty("account", account);
                        d.addProperty("songId", song.getId());
                        d.addProperty("songSrc", song.getSrc());
                        o.add("data", d);
                        try {
                            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                            writer.write(o.toString()+"\n");
                            writer.flush();
                            InputStreamReader inputStreamReader=new InputStreamReader(in);
                            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                            String line = bufferedReader.readLine();
                            Message msg = new Message(line);
                            boolean starred = msg.body().get("star").getAsBoolean();
                            song.setStarred(starred);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        localSongs.add(song);
                    } else if(mp3file.hasId3v1Tag()) {
                        ID3v1 id3v1Tag = mp3file.getId3v1Tag();
                        song.setSinger(id3v1Tag.getArtist());
                        song.setTitle(id3v1Tag.getTitle());
                        song.setAlbum(id3v1Tag.getAlbum());
                        song.setDownloaded(true);
                        song.setLocalurl(songpath);
                        song.setLength(mp3file.getLengthInSeconds());
                        song.setSrc(src);
                        song.setId((it.getName().substring(0,it.getName().lastIndexOf("."))));
                        localSongs.add(song);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void freshLoveSongs() {
        loveSongs.clear();
        JsonObject o = new JsonObject();
        JsonObject d = new JsonObject();
        o.addProperty("type", 2);
        d.addProperty("account", account);
        o.add("data", d);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(o.toString()+"\n");
            writer.flush();
            InputStreamReader inputStreamReader=new InputStreamReader(in);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            Message msg = new Message(line);
            addLoveSongs(msg.body());
            String[] head = new String[]{"","音乐标题", "歌手", "专辑", "时长"};
            DefaultTableModel tableModel=new DefaultTableModel(queryData2(head.length),head){
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };
            tableLove.setModel(tableModel);
            tableLove.getColumnModel().getColumn(0).setPreferredWidth(40);
            tableLove.getColumnModel().getColumn(1).setPreferredWidth(280);
            tableLove.getColumnModel().getColumn(2).setPreferredWidth(130);
            tableLove.getColumnModel().getColumn(3).setPreferredWidth(130);
            tableLove.getColumnModel().getColumn(4).setPreferredWidth(60);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addLoveSongs(JsonObject o) {
        JsonArray array = o.getAsJsonArray("song");
        for(JsonElement e : array) {
            SongInfo song = new SongInfo();
            JsonObject o2 = e.getAsJsonObject();
            song.setId(StringUtils.strip(o2.get("mid").toString(), "\""));
            song.setSrc(o2.get("src").getAsInt());
            song.setUrl(StringUtils.strip(o2.get("url").toString(), "\""));
            song.setTitle(StringUtils.strip(o2.get("title").toString(), "\""));
            song.setSinger(StringUtils.strip(o2.get("singer").toString(), "\""));
            song.setAlbum(StringUtils.strip(o2.get("album").toString(), "\""));
            song.setLength(o2.get("length").getAsInt());
            song.setStarred(true);
            loveSongs.add(song);
        }
    }

    private void freshSearchSongs() {
        searchSongs.clear();
        JsonObject o = new JsonObject();
        JsonObject d = new JsonObject();
        o.addProperty("type", 6);
        d.addProperty("word", search.getText());
        o.add("data", d);
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
            writer.write(o.toString()+"\n");
            writer.flush();
            InputStreamReader inputStreamReader=new InputStreamReader(in);
            BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
            String line = bufferedReader.readLine();
            Message msg = new Message(line);
            addSearchSongs(msg.body());
            String[] head = new String[]{"","音乐标题", "歌手", "专辑", "时长"};
            DefaultTableModel tableModel=new DefaultTableModel(queryData3(head.length),head){
                public boolean isCellEditable(int row, int column)
                {
                    return false;
                }
            };
            tableSearch.setModel(tableModel);
            tableSearch.getColumnModel().getColumn(0).setPreferredWidth(40);
            tableSearch.getColumnModel().getColumn(1).setPreferredWidth(280);
            tableSearch.getColumnModel().getColumn(2).setPreferredWidth(130);
            tableSearch.getColumnModel().getColumn(3).setPreferredWidth(130);
            tableSearch.getColumnModel().getColumn(4).setPreferredWidth(60);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void addSearchSongs(JsonObject o) {
        JsonArray array = o.getAsJsonArray("song");
        for(JsonElement e : array) {
            SongInfo song = new SongInfo();
            JsonObject o2 = e.getAsJsonObject();
            song.setId(StringUtils.strip(o2.get("mid").toString(), "\""));
            song.setSrc(o2.get("src").getAsInt());
            song.setUrl(StringUtils.strip(o2.get("url").toString(), "\""));
            song.setTitle(StringUtils.strip(o2.get("title").toString(), "\""));
            song.setSinger(StringUtils.strip(o2.get("singer").toString(), "\""));
            song.setAlbum(StringUtils.strip(o2.get("album").toString(), "\""));
            song.setLength(o2.get("length").getAsInt());
            song.setImages(StringUtils.strip(o2.get("image").toString(), "\""));
            JsonObject o3 = new JsonObject();
            JsonObject d = new JsonObject();
            o3.addProperty("type", 3);
            d.addProperty("account", account);
            d.addProperty("songId", song.getId());
            d.addProperty("songSrc", song.getSrc());
            o3.add("data", d);
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(o3.toString()+"\n");
                writer.flush();
                InputStreamReader inputStreamReader=new InputStreamReader(in);
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                Message msg = new Message(line);
                boolean starred = msg.body().get("star").getAsBoolean();
                song.setStarred(starred);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            searchSongs.add(song);
        }
    }

    public Object[][] queryData(int length){
        Object[][] data=new Object[localSongs.size()][length];

        for(int i=0;i<localSongs.size();i++){
            data[i][0]=i+1;
            data[i][1]=localSongs.get(i).getTitle();
            data[i][2]=localSongs.get(i).getSinger();
            data[i][3]=localSongs.get(i).getAlbum();
            long time = localSongs.get(i).getLength();
            data[i][4]=time/60+":"+String.format("%02d", time%60);
        }
        return data;
    }

    public Object[][] queryData2(int length){
        Object[][] data=new Object[loveSongs.size()][length];

        for(int i=0;i<loveSongs.size();i++){
            data[i][0]=i+1;
            data[i][1]=loveSongs.get(i).getTitle();
            data[i][2]=loveSongs.get(i).getSinger();
            data[i][3]=loveSongs.get(i).getAlbum();
            long time = loveSongs.get(i).getLength();
            data[i][4]=time/60+":"+String.format("%02d", time%60);
        }
        return data;
    }

    public Object[][] queryData3(int length){
        Object[][] data=new Object[searchSongs.size()][length];

        for(int i=0;i<searchSongs.size();i++){
            data[i][0]=i+1;
            data[i][1]=searchSongs.get(i).getTitle();
            data[i][2]=searchSongs.get(i).getSinger();
            data[i][3]=searchSongs.get(i).getAlbum();
            long time = searchSongs.get(i).getLength();
            data[i][4]=time/60+":"+String.format("%02d", time%60);
        }
        return data;
    }
    
    public void login() {
        // 创建 JFrame 实例
        JFrame frame = new JFrame("JHPlayer");
        // Setting the width and height of frame
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */

        JPanel panel = new JPanel();
        // 添加面板
        frame.add(panel);
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        placeComponentsLogin(panel,frame);

        // 设置界面可见
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    public void init() {
        try {
            Socket s = new Socket("127.0.0.1",MyServer.PORT);
            //构建IO
            in = s.getInputStream();
            out = s.getOutputStream();
            picFavorite = new ImageIcon("src/main/resources/收藏.png");
            picFavorite.setImage(picFavorite.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
            picUnFavorite = new ImageIcon("src/main/resources/未收藏.png");
            picUnFavorite.setImage(picUnFavorite.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
            picDownload = new ImageIcon("src/main/resources/下载.png");
            picDownload.setImage(picDownload.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
            picUnDownload = new ImageIcon("src/main/resources/未下载.png");
            picUnDownload.setImage(picUnDownload.getImage().getScaledInstance(20,20,Image.SCALE_SMOOTH));
            picPause = new ImageIcon("src/main/resources/暂停.png");
            picPause.setImage(picPause.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH));
            picPlay = new ImageIcon("src/main/resources/播放.png");
            picPlay.setImage(picPlay.getImage().getScaledInstance(25,25,Image.SCALE_SMOOTH));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void placeComponentsLogin(JPanel panel, JFrame frame) {

        /* 布局部分我们这边不多做介绍
         * 这边设置布局为 null
         */
        panel.setLayout(null);

        // 创建 JLabel
        JLabel userLabel = new JLabel("账户:");
        /* 这个方法定义了组件的位置。
         * setBounds(x, y, width, height)
         * x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小。
         */
        userLabel.setBounds(30,20,80,25);
        panel.add(userLabel);

        /*
         * 创建文本域用于用户输入
         */
        JTextField userText = new JTextField(20);
        userText.setBounds(100,20,165,25);
        panel.add(userText);

        // 输入密码的文本域
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(30,50,80,25);
        panel.add(passwordLabel);

        /*
         *这个类似用于输入的文本域
         * 但是输入的信息会以点号代替，用于包含密码的安全性
         */
        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);

        // 创建登录按钮
        JButton loginButton = new JButton("登录");
        loginButton.setBounds(70, 100, 80, 25);
        loginButton.addActionListener(e -> {
            String id = userText.getText();
            String pwd = new String(passwordText.getPassword());
            if(id.equals("") || pwd.equals("")) {
                infoMsg("输入不能为空");return;
            }
            int type = 0;
            JsonObject o = new JsonObject();
            JsonObject d = new JsonObject();
            o.addProperty("type", type);
            d.addProperty("account", id);
            d.addProperty("pwd", pwd);
            o.add("data", d);
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(o.toString()+"\n");
                writer.flush();
                InputStreamReader inputStreamReader=new InputStreamReader(in);
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                Message msg = new Message(line);
                if(msg.type()==0 && msg.body().get("state").getAsInt()==1) {
                    account = id;
                    PlayerWindow();
                    frame.dispose();
                } else if(msg.type()==0 && msg.body().get("state").getAsInt()==0) {
                    infoMsg("fail");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        panel.add(loginButton);
        JButton signupButton = new JButton("注册");
        signupButton.setBounds(180, 100, 80, 25);
        signupButton.addActionListener(e -> {
            register();
            frame.dispose();

        });
        panel.add(signupButton);
    }

    public void register() {
        // 创建 JFrame 实例
        JFrame frame = new JFrame("JHPlayer");
        // Setting the width and height of frame
        frame.setSize(350, 200);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /* 创建面板，这个类似于 HTML 的 div 标签
         * 我们可以创建多个面板并在 JFrame 中指定位置
         * 面板中我们可以添加文本字段，按钮及其他组件。
         */
        JPanel panel = new JPanel();
        // 添加面板
        frame.add(panel);
        /*
         * 调用用户定义的方法并添加组件到面板
         */
        placeComponentsSignup(panel, frame);

        // 设置界面可见
        frame.setLocationRelativeTo(frame.getOwner());
        frame.setVisible(true);
    }

    private void placeComponentsSignup(JPanel panel, JFrame frame) {
        panel.setLayout(null);

        /* 这个方法定义了组件的位置。
         * setBounds(x, y, width, height)
         * x 和 y 指定左上角的新位置，由 width 和 height 指定新的大小。
         */
        JLabel userLabel = new JLabel("账户:");
        userLabel.setBounds(30,20,80,25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100,20,165,25);
        panel.add(userText);

        // 输入密码的文本域
        JLabel passwordLabel = new JLabel("密码:");
        passwordLabel.setBounds(30,50,80,25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100,50,165,25);
        panel.add(passwordText);

        // 确认密码的文本域
        JLabel passwordConfirmLabel = new JLabel("确认密码:");
        passwordConfirmLabel.setBounds(30,80,80,25);
        panel.add(passwordConfirmLabel);

        JPasswordField passwordTextConfirm = new JPasswordField(20);
        passwordTextConfirm.setBounds(100,80,165,25);
        panel.add(passwordTextConfirm);


        // 创建注册按钮
        JButton signupButton = new JButton("注册");
        signupButton.setBounds(70, 130, 80, 25);
        signupButton.addActionListener(e -> {
            String id = userText.getText();
            String pwd = new String(passwordText.getPassword());
            String pwd2 = new String(passwordTextConfirm.getPassword());
            if(id.equals("") || pwd.equals("") || pwd2.equals("")) {
                infoMsg("输入不能为空");return;
            }
            if(!pwd.equals(pwd2)) {
                infoMsg("两次密码输入不一致");return;
            }
            int type = 1;
            JsonObject o = new JsonObject();
            JsonObject d = new JsonObject();
            o.addProperty("type", type);
            d.addProperty("account", id);
            d.addProperty("pwd", pwd);
            o.add("data", d);
            try {
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(out));
                writer.write(o.toString()+"\n");
                writer.flush();
                InputStreamReader inputStreamReader=new InputStreamReader(in);
                BufferedReader bufferedReader=new BufferedReader(inputStreamReader);
                String line = bufferedReader.readLine();
                Message msg = new Message(line);
                if(msg.type()==1 && msg.body().get("state").getAsInt()==1) {
                    infoMsg("注册成功");
                } else if(msg.type()==1 && msg.body().get("state").getAsInt()==0) {
                    infoMsg(StringUtils.strip(msg.body().get("msg").toString(),"\""));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        panel.add(signupButton);
        JButton backButton = new JButton("返回");
        backButton.setBounds(180, 130, 80, 25);
        backButton.addActionListener(e -> {
            login();
            frame.dispose();

        });
        panel.add(backButton);
    }

    public static void infoMsg(String msg) {
        JOptionPane.showMessageDialog(null, msg, "提示信息",JOptionPane.INFORMATION_MESSAGE);
    }

    class counterThread extends Thread {
        int counter;
        boolean running;
        public counterThread(){
            counter = 0;
            running = true;
        }

        public void run() {
            while(running) {
                if(playing) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    counter++;
                    currentTime.setText(String.format("%02d", counter/60)+":"+String.format("%02d", counter%60));
                }
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopThread() {
            running = false;
        }

    }


}

class MyTable extends JTable {

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (getFillsViewportHeight()) {
            paintEmptyRows(g);
        }
    }

    /**
     * Paints the backgrounds of the implied empty rows when the table model is
     * insufficient to fill all the visible area available to us. We don't
     * involve cell renderers, because we have no data.
     */
    protected void paintEmptyRows(Graphics g) {
        final int rowCount = getRowCount();
        final Rectangle clip = g.getClipBounds();
        if (rowCount * rowHeight < clip.height) {
            for (int i = rowCount; i <= clip.height / rowHeight; ++i) {
                g.setColor(colorForRow(i));
                g.fillRect(clip.x, i * rowHeight, clip.width, rowHeight);
            }
        }
    }

    /**
     * Returns the appropriate background color for the given row.
     */
    protected Color colorForRow(int row) {
        return (row % 2 == 0) ? Color.WHITE : Color.decode("#FFFDF8");
    }

    /**
     * Shades alternate rows in different colors.
     */
    @Override
    public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component c = super.prepareRenderer(renderer, row, column);
        if (isCellSelected(row, column) == false) {
            c.setBackground(colorForRow(row));
            c.setForeground(UIManager.getColor("Table.foreground"));
        } else {
            c.setBackground(UIManager.getColor("Table.selectionBackground"));
            c.setForeground(UIManager.getColor("Table.selectionForeground"));
        }
        return c;
    }
}


