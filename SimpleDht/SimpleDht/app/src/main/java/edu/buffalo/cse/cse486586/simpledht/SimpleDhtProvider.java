package edu.buffalo.cse.cse486586.simpledht;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Formatter;
import java.util.concurrent.ExecutionException;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;


public class SimpleDhtProvider extends ContentProvider {
    private static final String TAG = SimpleDhtProvider.class.getName();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private  static  final String JOIN="joining";
    private  static  final String INSERT="insert";
    private static  final String UPDATING="updating";
    private static  final String QUERY="query";
    private  static  final String QUERYUPDATE="queryupdate";
    private static  final String DELETE="delete";
    private  static  final String DELETEUPDATE="deleteupdate";
    private static  final String STARQUERY="starquery";
    private  static  final String STARQUERYUPDATE="starqueryupdate";
    static boolean found=false;
    public String mycurrentnodeid="111";
    public String mypredecessor="111";
    public String mysuccessor="111";
    public String valueToRead;
    static final int SERVER_PORT = 10000;
    ArrayList<DHTclass> list=new ArrayList<DHTclass>();
    ArrayList<String> ports = new ArrayList<String>();
    static String myclientport="";
    MatrixCursor localkey = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
    Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledht.provider");

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub

        if (selection.equals("@")) {
            String[] filesList = getContext().fileList();
            for (String s : filesList) {
                getContext().deleteFile(s);
            }
        } else if (selection.equals("*")) {
            String[] filesList = getContext().fileList();
            for (String s : filesList) {
                getContext().deleteFile(s);
                    }

        } else{
            if(mycurrentnodeid=="111"||mypredecessor=="111"||mypredecessor=="112") {
                getContext().deleteFile(selection);
            }
            else{
                String msggg="";
                String msgd="";
                try{
                    Socket socketd = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt("11108"));
                    DataOutputStream cl_pwd2= new DataOutputStream(socketd.getOutputStream());
                    String msgts=DELETE+"##&"+selection+"##&"+null;
                    cl_pwd2.writeUTF(msgts);
                    cl_pwd2.flush();
                    DataInputStream bfrd1 = new DataInputStream(socketd.getInputStream());
                    msggg=bfrd1.readUTF();
                    Log.i("stringkey",msggg);
                    socketd.close();
                }
                catch (Exception e) {
                    Log.e("ClientTaskException", e.toString());
                }
                try{
                    String []keyval=msggg.split("##&");
                    Socket socket1d = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(keyval[0]));
                    DataOutputStream cl_pw2d= new DataOutputStream(socket1d.getOutputStream());
                    String msgts=DELETEUPDATE+"##&"+keyval[1]+"##&"+null;
                    cl_pw2d.writeUTF(msgts);
                    cl_pw2d.flush();
                    DataInputStream bfr11 = new DataInputStream(socket1d.getInputStream());
                    msgd=bfr11.readUTF();
                    Log.i("delete",msgd);
                    if(msgd.equals("DeleteAcknow")){
                    socket1d.close();}

                }
                catch (Exception e) {
                    Log.e("ClientTaskException", e.toString());
                }
            }
        }
        return 0;
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {

        // TODO Auto-generated method stub
        String filename = (String) values.get(KEY_FIELD);
        String string = (String) values.get(VALUE_FIELD);
        Log.i("insideinsert",mycurrentnodeid+mysuccessor+mypredecessor);
        if(mycurrentnodeid!="111") {
            String keyhasvalue=getHashValue(filename);
            if (mypredecessor=="112"||mysuccessor=="112"||(keyhasvalue.compareTo(getHashValue(Integer.toString(Integer.parseInt(mycurrentnodeid)/2)))<=0
                    &&keyhasvalue.compareTo(getHashValue(Integer.toString(Integer.parseInt(mypredecessor)/2)))>0)||checkthepartition(filename) ){
                Log.i("thisif", "thisif");
                FileOutputStream outputStream;
                try {
                    outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                    outputStream.write(string.getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    Log.e(TAG, "File write failed");
                }
            } else  {
                Log.i("thiselseif", "thiselseif");
                String msg = INSERT + "##&" +mysuccessor+"##&"+filename+"##"+string;
                new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myclientport);
            }
        }
        else {
            Log.i("thiselse","thiselse");
            FileOutputStream outputStream;
            try {
                outputStream = getContext().openFileOutput(filename, Context.MODE_PRIVATE);
                outputStream.write(string.getBytes());
                outputStream.close();
            } catch (Exception e) {
                Log.e(TAG, "File write failed");
            }
        }


        Log.v("insert", values.toString());
        return uri;
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myclientport=myPort;
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            return false;
        }
        if(myPort=="11108") {
            ports.add(myPort);
            DHTclass obj = new DHTclass();
            obj.id = myPort;
            obj.hashport = getHashValue(Integer.toString(Integer.parseInt(myPort) / 2));
            obj.successor = "1112";
            obj.predecessor = "1112";
            list.add(obj);
            Log.i("insideif", Integer.toString(ports.size()));

        }
        String msg = JOIN + "##&" + myPort + "##&" + null;
        Log.i("msgimitial", msg);
        new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);

        return false;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // TODO Auto-generated method stub

        FileInputStream inputStream;
        if (selection.equals("*")) {
            if(mycurrentnodeid=="111"||mypredecessor=="111"||mypredecessor=="112"){
                MatrixCursor matrixCursor1 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                String[] filesList = getContext().fileList();
                for (String s : filesList) {
                    try {
                        Log.i(TAG, selection);
                        inputStream = getContext().openFileInput(s);
                        BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
                        valueToRead = bfr.readLine();
                        inputStream.close();
                        matrixCursor1.addRow(new Object[]{s, valueToRead});
                    } catch (Exception e) {
                        Log.e(TAG, "Query Failed");
                    }

                }
                return  matrixCursor1;
            }
            else{
                MatrixCursor matrixCursor10 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                String msdsag="";
                try{
                    Socket sockets = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt("11108"));
                    DataOutputStream cl_pw2= new DataOutputStream(sockets.getOutputStream());
                    String msgts=STARQUERY+"##&"+selection+"##&"+null;
                    cl_pw2.writeUTF(msgts);
                    cl_pw2.flush();
                    DataInputStream bfr1 = new DataInputStream(sockets.getInputStream());
                    msdsag=bfr1.readUTF();
                    Log.i("stringkey",msdsag);
                    sockets.close();
                }
                catch (Exception e) {
                    Log.e("ClientTaskException", e.toString());
                }
                String [] porteees=msdsag.split("##&");
                for(String remoteport:porteees){
                    if(remoteport!=null){
                        try{
                            Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(remoteport));
                            DataOutputStream cl_pw3= new DataOutputStream(socket1.getOutputStream());
                            String msgts=STARQUERYUPDATE+"##&"+remoteport+"##&"+null;
                            cl_pw3.writeUTF(msgts);
                            cl_pw3.flush();
                            DataInputStream bfr23 = new DataInputStream(socket1.getInputStream());
                            String msg1=bfr23.readUTF();
                            String [] splitss=msg1.split("##&");
                            for(String sss:splitss){
                                if(!sss.equals("SPEC")){
                                    String[] thirdsplit=sss.split("##");
                                    Log.i("splitkey",sss);
                                    matrixCursor10.addRow(new Object[]{thirdsplit[0],thirdsplit[1]});
                                }
                            }
                            Log.i("stringkey2",msg1);
                            socket1.close();

                        }
                        catch (Exception e) {
                            Log.e("ClientTaskException", e.toString());
                        }
                    }}
                return matrixCursor10;
            }

        } else if (selection.equals("@")) {
            MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
            String[] filesList = getContext().fileList();
            for (String s : filesList) {
                try {
                    Log.i(TAG, selection);
                    inputStream = getContext().openFileInput(s);
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
                    valueToRead = bfr.readLine();
                    Log.i("here", valueToRead);
                    inputStream.close();
                    matrixCursor2.addRow(new Object[]{s, valueToRead});
                } catch (Exception e) {
                    Log.e(TAG, "Query Failed");
                }

            }
            return  matrixCursor2;
        } else {
            if(mycurrentnodeid=="111"||mypredecessor=="112"||mypredecessor=="111") {
                MatrixCursor matrixCursor3 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                try {
                    Log.i(TAG, selection);
                    inputStream = getContext().openFileInput(selection);
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
                    valueToRead = bfr.readLine();
                    inputStream.close();
                    matrixCursor3.addRow(new Object[]{selection, valueToRead});
                } catch (Exception e) {
                    Log.e(TAG, "Query Failed");
                }
                Log.v("query", selection);

                return matrixCursor3;
            }
            else{
                Log.i("af","here");
                Log.i("dsad",selection);
                found=checkthepartition(selection);
                Log.i("found",Boolean.toString(found));
                if(found){
                    MatrixCursor matrixCursor4 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                    try {
                        Log.i(TAG, selection);
                        inputStream = getContext().openFileInput(selection);
                        BufferedReader bfr = new BufferedReader(new InputStreamReader(inputStream));
                        valueToRead = bfr.readLine();
                        inputStream.close();
                        matrixCursor4.addRow(new Object[]{selection, valueToRead});
                    } catch (Exception e) {
                        Log.e(TAG, "Query Failed");
                    }
                    Log.v("query", selection);

                    return matrixCursor4;
                }
                else{
                    MatrixCursor matrixCursor5 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                    String msg="";
                    try{
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt("11108"));
                        DataOutputStream cl_pw2= new DataOutputStream(socket.getOutputStream());
                        String msgts=QUERY+"##&"+selection+"##&"+null;
                        cl_pw2.writeUTF(msgts);
                        cl_pw2.flush();
                        DataInputStream bfr1 = new DataInputStream(socket.getInputStream());
                        msg=bfr1.readUTF();
                        Log.i("stringkey",msg);
                        socket.close();
                    }
                    catch (Exception e) {
                        Log.e("ClientTaskException", e.toString());
                    }
                    try{
                        String []keyval=msg.split("##&");
                        Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(keyval[0]));
                        DataOutputStream cl_pw2= new DataOutputStream(socket1.getOutputStream());
                        String msgts=QUERYUPDATE+"##&"+keyval[1]+"##&"+null;
                        cl_pw2.writeUTF(msgts);
                        cl_pw2.flush();
                        DataInputStream bfr = new DataInputStream(socket1.getInputStream());
                        String msg1=bfr.readUTF();
                        String[] fin=msg1.split("##&");
                        matrixCursor5.addRow(new Object[]{fin[0],fin[1]});
                        Log.i("stringkey2",msg1);
                        socket1.close();

                    }
                    catch (Exception e) {
                        Log.e("ClientTaskException", e.toString());
                    }
                    return matrixCursor5;
                }
            }
        }



    }
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            Log.i("socketaccepted","socket");
            String msgToRead = "";
            Socket s;
            try {
                while (true) {
                    s = serverSocket.accept();
                    Log.i("socketaccepted","socket2");
                    DataInputStream bfr = new DataInputStream(s.getInputStream());
                    msgToRead = bfr.readUTF();
                    Log.i("Readingmessagefrom",msgToRead);
                    String mes=msgToRead;
                    String[] messages=mes.split("##&");
                    if(messages[0].equals(JOIN)) {
                        DHTclass obj=new DHTclass();
                        String portnumber=messages[1];
                        Log.i("GotJoin",portnumber);
                        if(!ports.contains(portnumber)) {
                            ports.add(portnumber);
                            obj.id =portnumber;
                            obj.hashport=getHashValue(Integer.toString(Integer.parseInt(portnumber) / 2));
                            list.add(obj);
                        }
                        if(list.size()>0){
                        Collections.sort(list);
                        UpdatePredecessorAndSuccessor(list);
                        for (DHTclass st : list) {
                            Log.i("list", st.predecessor + "@" + st.id + "@" + st.successor);
                        }}
                        Log.i("myclientport",myclientport);
                        if(Integer.parseInt(myclientport)==11108 && list.size()>1){
                            for(DHTclass objec:list){
                                String msg1=UPDATING+"##&"+objec.predecessor+"##"+objec.id+"##&"+objec.successor;
                                Log.i("OKWHERE",msg1);
                                clientTaskToJoin(UPDATING,msg1) ;
                            }
                        }
                    }
                    else if(messages[0].equals(UPDATING)){
                        String []msg=messages[1].split("##");
                        String succ=messages[2];
                        mycurrentnodeid=msg[1];
                        mypredecessor=msg[0];
                        mysuccessor=succ;
                        Log.i("printingpred",mycurrentnodeid+mypredecessor+mysuccessor);
                    }
                    else if(messages[0].equals(INSERT)){
                        Log.i("insideinsertserver",messages[1]);
                        ContentValues vkeyValue = new ContentValues();
                        vkeyValue.put(KEY_FIELD, messages[1]);
                        vkeyValue.put(VALUE_FIELD,messages[2]);
                        insert(uri, vkeyValue);
                    }
                    else  if(messages[0].equals(QUERY)) {
                        Log.i("print",mes);
                        String finalport=null;
                        String key = messages[1];
                        if(list.size()>1) {
                            for (DHTclass lls : list) {
                                boolean check = checkparttitionQuery(key, lls.id);
                                if (check) {
                                    finalport = lls.id;
                                    break;
                                }
                            }
                        }
                        String stu=finalport+"##&"+key+"##&"+null;
                        DataOutputStream cl_pw= new DataOutputStream(s.getOutputStream());
                        cl_pw.writeUTF(stu);
                        cl_pw.flush();
                    }
                    else  if(messages[0].equals(QUERYUPDATE)) {
                        Log.i("printupdate",mes);
                        String key = messages[1];
                        String stu="";
                        FileInputStream inputStream=getContext().openFileInput(key);
                        BufferedReader bfrd = new BufferedReader(new InputStreamReader(inputStream));
                        String jesc = bfrd.readLine();
                        stu=key+"##&"+jesc;
                        inputStream.close();
                        DataOutputStream cl_pw2= new DataOutputStream(s.getOutputStream());
                        cl_pw2.writeUTF(stu);
                        cl_pw2.flush();
                    }
                    else if(messages[0].equals(STARQUERY)){
                        Log.i("printstarquery",mes);
                        String finalport="11108##&";
                        String key = messages[1];
                        for(DHTclass lls:list){
                            if(!lls.id.equals("11108")) {
                                finalport = finalport + lls.id+"##&";
                            }
                        }
                        String stu=finalport;
                        DataOutputStream cl_pw3= new DataOutputStream(s.getOutputStream());
                        cl_pw3.writeUTF(stu);
                        cl_pw3.flush();
                    }
                    else  if(messages[0].equals(STARQUERYUPDATE)) {
                        String[] filesList = getContext().fileList();
                        FileInputStream inputStream;
                        String ret="SPEC";
                        for (String f : filesList) {
                            Log.i(TAG,f);
                            inputStream = getContext().openFileInput(f);
                            BufferedReader bffr = new BufferedReader(new InputStreamReader(inputStream));
                            String sdsa = bffr.readLine();
                            ret=ret+"##&"+f+"##"+sdsa;
                            inputStream.close();
                        }
                        DataOutputStream cl_pw5= new DataOutputStream(s.getOutputStream());
                        cl_pw5.writeUTF(ret);
                        cl_pw5.flush();
                    }
                    else  if(messages[0].equals(DELETE)) {
                        Log.i("print",mes);
                        String finalport=null;
                        String key = messages[1];
                        for(DHTclass lls:list){
                            boolean check=checkparttitionQuery(key,lls.id);
                            if(check){
                                finalport=lls.id;
                                break;
                            }
                        }
                        String stu=finalport+"##&"+key+"##&"+null;
                        DataOutputStream cl_pwd= new DataOutputStream(s.getOutputStream());
                        cl_pwd.writeUTF(stu);
                        cl_pwd.flush();
                    }
                    else  if(messages[0].equals(DELETEUPDATE)) {
                        Log.i("deleteipdate",mes);
                        String key = messages[1];
                        getContext().deleteFile(key);
                        DataOutputStream cl_pwd1= new DataOutputStream(s.getOutputStream());
                        String std="DeleteAcknow";
                        cl_pwd1.writeUTF(std);
                        cl_pwd1.flush();
                    }
                   // s.close();
                }
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }
    }
    private class DHTclass implements Comparable<DHTclass> {
        String id;
        String hashport;
        String successor;
        String predecessor;

        DHTclass(String id, String hashport, String successor, String predecessor) {

            this.id = id;
            this.hashport = hashport;
            this.predecessor = predecessor;
            this.successor = successor;
        }

        DHTclass() {
        }
        public int compareTo (DHTclass otherObject) {
            return this.hashport.compareTo(otherObject.hashport);
        }
    }

    boolean checkthepartition(String keyvalue){
        String keyhash=getHashValue(keyvalue);
        if(mysuccessor=="112"||mypredecessor=="112"){Log.i("yesnullischecked","nullchecked");
        return true;}
        else {
            Log.i("herepart", mycurrentnodeid);
            String myhash = getHashValue(Integer.toString(Integer.parseInt(mycurrentnodeid) / 2));
            String mypredehash = getHashValue(Integer.toString(Integer.parseInt(mypredecessor) / 2));
            if (myhash.compareTo(keyhash) < 0 && mypredehash.compareTo(keyhash) < 0 && mypredehash.compareTo(myhash) > 0)
                return true;
            else if (myhash.compareTo(keyhash) >= 0 && mypredehash.compareTo(keyhash) < 0)
                return true;
            else if (myhash.compareTo(keyhash) > 0 && mypredehash.compareTo(keyhash) > 0 && mypredehash.compareTo(myhash) > 0)
                return true;
            else
                return false;
        }

    }
    boolean checkparttitionQuery(String keyvalue,String node){
        String keyhash=getHashValue(keyvalue);
        Log.i("herepart",node);
        DHTclass obj=getPredecessorAndSuccessor(node);
        String myhash = getHashValue(Integer.toString(Integer.parseInt(obj.id) / 2));
        String mypredehash = getHashValue(Integer.toString(Integer.parseInt(obj.predecessor) / 2));
        if (myhash.compareTo(keyhash) < 0 && mypredehash.compareTo(keyhash) < 0 && mypredehash.compareTo(myhash) > 0)
            return true;
        else if (myhash.compareTo(keyhash) >= 0 && mypredehash.compareTo(keyhash) < 0)
            return true;
        else if (myhash.compareTo(keyhash) > 0 && mypredehash.compareTo(keyhash) > 0 && mypredehash.compareTo(myhash) > 0)
            return true;
        else
            return false;
    }
    void UpdatePredecessorAndSuccessor(ArrayList<DHTclass> listtoUpdate) {
        if(listtoUpdate.size()==2)
        {
            DHTclass one=listtoUpdate.get(0);
            DHTclass two=listtoUpdate.get(1);
            one.successor=two.id;
            one.predecessor=two.id;
            two.successor=one.id;
            two.predecessor=one.id;

        }
        else if(listtoUpdate.size()==1){
            listtoUpdate.get(0).successor="112";
            listtoUpdate.get(0).predecessor="112";
        }
        else {
            for (int i=1;i<listtoUpdate.size()-1;i++){
                DHTclass temp1=listtoUpdate.get(i-1);
                DHTclass curr=listtoUpdate.get(i);
                DHTclass next=listtoUpdate.get(i+1);
                curr.predecessor=temp1.id;
                curr.successor=next.id;
            }
            DHTclass first=listtoUpdate.get(0);
            DHTclass last=listtoUpdate.get(listtoUpdate.size()-1);
            first.successor=listtoUpdate.get(1).id;
            first.predecessor=last.id;
            last.predecessor=listtoUpdate.get(listtoUpdate.size()-2).id;
            last.successor=first.id;
        }

    }
    void clientTaskToJoin(String type,String msg ){

        if(type.equals(UPDATING)){
            String msgtos=msg;
            Log.i("type",msg);
            new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msgtos,myclientport);
        }
        if(type.equals(QUERY)){
            String []dsd=msg.split("##&");
            String msgt=QUERY+"##&"+dsd[1]+dsd[2];
            new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR,msgt,myclientport);
        }

    }
    DHTclass getPredecessorAndSuccessor(String idport) {
        for (DHTclass ob:list){
            if(Integer.parseInt(idport)==Integer.parseInt(ob.id)){
                return ob;
            }
        }
        return null;
    }
    String getHashValue(String hasvale){
        String hashvalue="";
        try {
            hashvalue=genHash(hasvale);
        } catch (NoSuchAlgorithmException e) {
            Log.e("InHashfunctionge",e.toString());
        }
        return hashvalue;
    }
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }




    private class NodeClientTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... msgs) {
            String returnstring="";
            try {

                Log.i("Insidejoinclient",msgs[0]);
                String msgSend = msgs[0];
                String []msgToSend = msgSend.split("##&");

                if(msgToSend[0].equals(JOIN)){
                    Socket  socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt("11108"));
                    Log.i("msg",msgSend);
                    DataOutputStream cl_pw= new DataOutputStream(socket1.getOutputStream());
                    cl_pw.writeUTF(msgSend);
                    cl_pw.flush();
                    Thread.sleep(1000);
                    socket1.close();
                }
                else if(msgToSend[0].equals(UPDATING)){
                    String [] msg=msgToSend[1].split("##");
                    Socket  sock = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msg[1]));
                    String msg1=UPDATING+"##&"+msgToSend[1]+"##&"+msgToSend[2];
                    Log.i("updjoinmsgcl",msg1);
                    DataOutputStream   cl_pw= new DataOutputStream(sock.getOutputStream());
                    cl_pw.writeUTF(msg1);
                    cl_pw.flush();
                    Thread.sleep(1000);
                    sock.close();
                }
                else if(msgToSend[0].equals(INSERT)){
                    Log.i("hereinside",msgToSend[0]);
                    String remoteport=msgToSend[1];
                    String[] sst=msgToSend[2].split("##");
                    String msgs1=INSERT+"##&"+sst[0]+"##&"+sst[1];
                    Socket socket  = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(remoteport));
                    DataOutputStream cl_pw= new DataOutputStream(socket.getOutputStream());
                    cl_pw.writeUTF(msgs1);
                    cl_pw.flush();
                    Thread.sleep(1000);
                    socket.close();
                }
            } catch (Exception e) {
                Log.e("ClientTaskException", e.toString());
            }
            return returnstring;
        }
    }
}
