package server;

import search.NeteaseSearch;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import static Utils.dbConnecter.*;

public class MyServer {
    public static NeteaseSearch ns = new NeteaseSearch();
    public static final int PORT = 2443;
    public static ArrayList<ServerThread>list =new ArrayList<ServerThread>();

    public static void main(String[] args) {
        MyServer server = new MyServer();
        server.initServer();
    }

    public void initServer() {

        try {
            ServerSocket server = new ServerSocket(PORT);
            System.out.println("server established......");
            db_connect();
            //不断获取客户端的连接
            while(true){
                Socket socket =server.accept();
                System.out.println("client connecting......");
                //当有客户端连接进来以后，开启一个线程，用来处理该客户端的逻辑,
                ServerThread st = new ServerThread(socket);
                st.start();
                //添加该客户端到容器中
                list.add(st);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
