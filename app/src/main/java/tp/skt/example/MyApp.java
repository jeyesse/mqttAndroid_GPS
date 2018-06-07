package tp.skt.example;

import android.app.Application;
import android.util.Log;

import java.util.HashMap;

import tp.skt.example.NodeData;

public class MyApp extends Application {
    HashMap<String, NodeData> nodeMap = new HashMap<String , NodeData>();

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("Application create", "Application onCreate() called");
    }

    public HashMap<String, NodeData> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(HashMap<String, NodeData> nodeMap) {
        this.nodeMap = nodeMap;
    }
}
