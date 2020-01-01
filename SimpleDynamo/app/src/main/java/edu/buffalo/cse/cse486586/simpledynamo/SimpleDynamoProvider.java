package edu.buffalo.cse.cse486586.simpledynamo;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
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
import android.content.res.Resources;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.telephony.TelephonyManager;
import android.util.Log;

public class SimpleDynamoProvider extends ContentProvider {

    private static final String TAG = SimpleDynamoProvider.class.getName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;
    public String [] Remote_PortsList= new String[]{REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};
    ArrayList<DHTclass> ringlist=new ArrayList<DHTclass>();
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    private  static  final String INSERT="insert";
    private static  final String QUERY="query";
    private static  final String DELETE="delete";
    private static final String STARQUERY="starquery";
    static String myclientport="";
    boolean chekingdummy=false;
    private static final String PREDECESSORQUERY="predeceq";
    private static  final  String SUCCESSORQUERY="successorquer";
    private static final String ONCREATEINSERT="oncretins";
    private static final String QUERYKEY="querykey";
    private static final String INITIALDELETEINSERT="initial";
    boolean recoverflag=false;
    boolean deleteflag=false;
    Uri uri = buildUri("content", "edu.buffalo.cse.cse486586.simpledynamo.provider");
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        synchronized (this) {
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

            } else {
                Log.i("insidedelno@*", selection);
                String portfoundqdeleteside = "";
                for (String sl : Remote_PortsList) {
                    boolean found = checkparttitionQuery(selection, sl);
                    if (found) {
                        portfoundqdeleteside = sl;
                        Log.i("itreachedheredel", portfoundqdeleteside);
                        break;
                    }
                }
                DHTclass objd1 = getPredecessorAndSuccessor(portfoundqdeleteside);
                String firstsuccd = objd1.successor;
                DHTclass objd2 = getPredecessorAndSuccessor(firstsuccd);
                String secondsuccd = objd2.successor;
                if(recoverflag){try{Thread.sleep(2000);}catch(Exception e ){Log.e("sleepissue",e.toString());}}
                else{
                    try {
                        Socket socket2d = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(portfoundqdeleteside));
                        DataOutputStream cl_pw2d2 = new DataOutputStream(socket2d.getOutputStream());
                        String msgtsd = DELETE + "##&" + selection + "##&" + null;
                        cl_pw2d2.writeUTF(msgtsd);
                        cl_pw2d2.flush();
                        DataInputStream bfr11 = new DataInputStream(socket2d.getInputStream());
                        msgtsd = bfr11.readUTF();
                        Log.i("delete", msgtsd);
                        if (msgtsd.equals("DeleteAcknow")) {
                            socket2d.close();
                        }

                    }catch (SocketTimeoutException st) {
                        Log.e("exception",st.toString());
                    } catch (StreamCorruptedException e) {
                        Log.e("exception",e.toString());
                    } catch (FileNotFoundException e) {
                        Log.e("exception",e.toString());
                    }catch (EOFException e) {
                        Log.e("exception",e.toString());
                    }catch (UnknownHostException e) {
                        Log.e("exception",e.toString());
                    } catch (IOException e) {
                        Log.e("exception",e.toString());
                    }catch(NullPointerException e){
                        Log.e("exceptionnull",e.toString());
                    } catch (Exception e) {
                        Log.e("exception",e.toString());
                    }
                    try {

                        Socket socket2df = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(firstsuccd));
                        DataOutputStream cl_pw2d2f = new DataOutputStream(socket2df.getOutputStream());
                        String msgtsdf = DELETE + "##&" + selection + "##&" + null;
                        cl_pw2d2f.writeUTF(msgtsdf);
                        cl_pw2d2f.flush();
                        DataInputStream bfr11 = new DataInputStream(socket2df.getInputStream());
                        msgtsdf = bfr11.readUTF();
                        Log.i("delete", msgtsdf);
                        if (msgtsdf.equals("DeleteAcknow")) {
                            socket2df.close();
                        }

                    } catch (SocketTimeoutException st) {
                        Log.e("exception",st.toString());
                    } catch (StreamCorruptedException e) {
                        Log.e("exception",e.toString());
                    } catch (FileNotFoundException e) {
                        Log.e("exception",e.toString());
                    }catch (EOFException e) {
                        Log.e("exception",e.toString());
                    }catch (UnknownHostException e) {
                        Log.e("exception",e.toString());
                    } catch (IOException e) {
                        Log.e("exception",e.toString());
                    }catch(NullPointerException e){
                        Log.e("exceptionnull",e.toString());
                    } catch (Exception e) {
                        Log.e("exception",e.toString());
                    }
                    try {

                        Socket socket2ds = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(secondsuccd));
                        DataOutputStream cl_pw2d2s = new DataOutputStream(socket2ds.getOutputStream());
                        String msgtsds = DELETE + "##&" + selection + "##&" + null;
                        cl_pw2d2s.writeUTF(msgtsds);
                        cl_pw2d2s.flush();
                        DataInputStream bfr11 = new DataInputStream(socket2ds.getInputStream());
                        msgtsds = bfr11.readUTF();
                        Log.i("delete", msgtsds);
                        if (msgtsds.equals("DeleteAcknow")) {
                            socket2ds.close();
                        }

                    } catch (SocketTimeoutException st) {
                        Log.e("exception",st.toString());
                    } catch (StreamCorruptedException e) {
                        Log.e("exception",e.toString());
                    } catch (FileNotFoundException e) {
                        Log.e("exception",e.toString());
                    }catch (EOFException e) {
                        Log.e("exception",e.toString());
                    }catch (UnknownHostException e) {
                        Log.e("exception",e.toString());
                    } catch (IOException e) {
                        Log.e("exception",e.toString());
                    }catch(NullPointerException e){
                        Log.e("exceptionnull",e.toString());
                    } catch (Exception e) {
                        Log.e("exception",e.toString());
                    }

                }
            }
            return 0;
        }
    }

    @Override
    public String getType(Uri uri) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        synchronized (this){
            // TODO Auto-generated method stub
            String filename = (String) values.get(KEY_FIELD);
            String string = (String) values.get(VALUE_FIELD);
            String initiali=Integer.toString(1)+"_"+string;
            Log.i("ini",initiali);
            //String keyhasvalue=getHashValue(filename);
            String portwherelies=null;
            for(String sl:Remote_PortsList){
                boolean found=checkparttitionQuery(filename,sl);
                if(found){
                    portwherelies=sl;
                    Log.i("itreachedhere",portwherelies);
                    break;
                }
            }

            DHTclass obj=getPredecessorAndSuccessor(portwherelies);
            String firs_succ=obj.successor;
            DHTclass obj2=getPredecessorAndSuccessor(firs_succ);
            String sec_succ=obj2.successor;
            Log.i("whatsucc",portwherelies+firs_succ+sec_succ);
            // String msgdummy=INSERT+"##&"+myclientport+"##&"+"123dummy123"+"##"+"1_checkingdummyinsert";
            //AsyncTask<String, Void, String> insert_dummy=new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgdummy,myclientport);
            //try {
            //  String s=insert_dummy.get();
            // Log.i(TAG,s);
            //} catch (InterruptedException e) {
            //  e.printStackTrace();
            //} catch (ExecutionException e) {
            //  e.printStackTrace();
            //}
            if(recoverflag){try{Thread.sleep(2500);}catch(Exception e ){Log.e("sleepissue",e.toString());}}
            else{
                String msg0=INSERT+"##&"+portwherelies+"##&"+filename+"##"+initiali;
                AsyncTask<String, Void, String> zero_mess=new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg0,myclientport);
                try {
                    String s=zero_mess.get();
                    Log.i(TAG,s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                String msg=INSERT+"##&"+firs_succ+"##&"+filename+"##"+initiali;
                AsyncTask<String, Void, String> first_mess=new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg,myclientport);
                try {
                    String s=first_mess.get();
                    Log.i(TAG,s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                String msg2=INSERT+"##&"+sec_succ+"##&"+filename+"##"+initiali;
                AsyncTask<String, Void, String> sec_mess=new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg2,myclientport);
                try {
                    String s=sec_mess.get();
                    Log.i(TAG,s);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }catch(NullPointerException e){
                    e.printStackTrace();
                }
                Log.v("insert",initiali);}
            return uri;}
    }

    @Override
    public boolean onCreate() {
        // TODO Auto-generated method stub
        TelephonyManager tel = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        myclientport=myPort;
        DHTclass obj;
        for (String plist:Remote_PortsList){
            obj= new DHTclass();
            obj.id=plist;
            obj.hashport=getHashValue(Integer.toString(Integer.parseInt(plist) / 2));
            ringlist.add(obj);
        }
        Collections.sort(ringlist);
        UpdatePredecessorAndSuccessor(ringlist);
        for(DHTclass st:ringlist){
            Log.i("printinglist",st.predecessor+st.id+st.successor);
        }
        DHTclass current=getPredecessorAndSuccessor(myclientport);
        DHTclass preddcessorobj=getPredecessorAndSuccessor(current.predecessor);
        DHTclass sucsseorobj=getPredecessorAndSuccessor(current.successor);
        try {
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        } catch (IOException e) {
            Log.e(TAG, e.toString());
            // return false;
        }

            String initial_check=INITIALDELETEINSERT+"##&"+myclientport+"##&"+null;
            AsyncTask<String, Void, String> zero_onecre=new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, initial_check,myclientport);
            try {
                String s=zero_onecre.get();
                chekingdummy=Boolean.parseBoolean(s);
                Log.i(TAG,s);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
        Log.i("usfasfas",String.valueOf(chekingdummy));
        if(chekingdummy){
            recoverflag=true;
            String zerovalues = "SPEC" + "##";
            String msg_one = PREDECESSORQUERY + "##&" +current.predecessor + "##&" + preddcessorobj.predecessor;
            AsyncTask<String, Void, String> zero_one = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_one, myclientport);
            try {
                zerovalues = zerovalues + zero_one.get();
                Log.i(TAG, zerovalues);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            Log.i("onepredecessor",zerovalues);
            String msg_two = ONCREATEINSERT + "##&" + current.id + "##&" + zerovalues;
            AsyncTask<String, Void, String> zero_two = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_two, myclientport);
            try {
                String vals = zero_two.get();
                Log.i("finised", "finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            String onevalues = "SPEC" + "##";
            String msg_three = SUCCESSORQUERY + "##&" +current.successor + "##&" +current.id;
            AsyncTask<String, Void, String> zero_three = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_three, myclientport);
            try {
                onevalues = onevalues + zero_three.get();
                Log.i(TAG, onevalues);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            Log.i("onesuccessor",onevalues);
            String msg_four = ONCREATEINSERT + "##&" + current.id + "##&" + onevalues;
            AsyncTask<String, Void, String> zero_four = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_four, myclientport);
            try {
                String vals = zero_four.get();
                Log.i("finised", "finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            //Second round of Predecessor and succesor.
            String twovalues = "SPEC" + "##";
            String msg_five = PREDECESSORQUERY + "##&" +preddcessorobj.predecessor + "##&" + preddcessorobj.successor;
            AsyncTask<String, Void, String> zero_five = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_five, myclientport);
            try {
                twovalues = twovalues + zero_five.get();
                Log.i(TAG, twovalues);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            Log.i("twopredecessor",twovalues);
            String msg_six = ONCREATEINSERT + "##&" + current.id + "##&" + twovalues;
            AsyncTask<String, Void, String> zero_six = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_six, myclientport);
            try {
                String vals = zero_six.get();
                Log.i("finised", "finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            String threevalues = "SPEC" + "##";
            String msg_seven = SUCCESSORQUERY + "##&" +sucsseorobj.successor + "##&" +current.id;
            AsyncTask<String, Void, String> zero_seven = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_seven, myclientport);
            try {
                threevalues = threevalues + zero_seven.get();
                Log.i(TAG, threevalues);
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            Log.i("twosuccessor",threevalues);
            String msg_eight = ONCREATEINSERT + "##&" + current.id + "##&" + threevalues;
            AsyncTask<String, Void, String> zero_eight = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg_eight, myclientport);
            try {
                String vals = zero_eight.get();
                Log.i("finised", "finished");
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }catch(NullPointerException e){
                e.printStackTrace();
            }
            recoverflag=false;


        }

        return false;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // TODO Auto-generated method stub
        Log.i("whatsele", selection);
        synchronized (this){
            if (selection.equals("@")) {
                {try{Thread.sleep(2500);}catch(Exception e ){Log.e("sleepissue",e.toString());}}
                MatrixCursor matrixCursor1 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                String[] filesList = getContext().fileList();
                String valueToRedaQuery1;
                for (String s : filesList) {
                    Log.i("filelistreplic", s);
                    try {
                        Log.i(TAG, selection);
                        FileInputStream inputstreamquery1 = getContext().openFileInput(s);
                        BufferedReader bfrff = new BufferedReader(new InputStreamReader(inputstreamquery1));
                        valueToRedaQuery1 = bfrff.readLine();
                        bfrff.close();
                        inputstreamquery1.close();
                        if (!s.equals("123dummy123")) {
                            String[] splitatquery = valueToRedaQuery1.split("_");
                            Log.i("here", splitatquery[1]);
                            matrixCursor1.addRow(new Object[]{s, splitatquery[1]});} }
                    catch (Exception e) {
                        Log.e(TAG, "Query Failed");
                    }
                }
                return matrixCursor1;
            } else if (selection.equals("*")) {
                if(recoverflag){try{Thread.sleep(2000);}catch(Exception e ){Log.e("sleepissue",e.toString());}}
                MatrixCursor matrixCursorstar = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});

                for (String remoteport : Remote_PortsList) {
                    if (remoteport != null) {
                        try {
                            Socket socketstar = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                    Integer.parseInt(remoteport));
                            DataOutputStream cl_pwstar = new DataOutputStream(socketstar.getOutputStream());
                            String msgtstar = STARQUERY + "##&" + remoteport + "##&" + null;
                            cl_pwstar.writeUTF(msgtstar);
                            cl_pwstar.flush();
                            DataInputStream bfrstar = new DataInputStream(socketstar.getInputStream());
                            String msg1 = bfrstar.readUTF();
                            String[] splitss = msg1.split("##&");
                            for (String sss : splitss) {
                                if (!sss.equals("SPEC")) {
                                    String[] thirdsplit = sss.split("##");
                                    if (!thirdsplit[0].equals("123dummy123")) {
                                        Log.i("splitkey", sss);
                                        String[] lastsplit = thirdsplit[1].split("_");
                                        matrixCursorstar.addRow(new Object[]{thirdsplit[0], lastsplit[1]});
                                    }
                                }
                            }
                            Log.i("stringkey2", msg1);
                            socketstar.close();

                        } catch (SocketTimeoutException st) {
                            Log.e("exception",st.toString());
                        } catch (StreamCorruptedException e) {
                            Log.e("exception",e.toString());
                        } catch (FileNotFoundException e) {
                            Log.e("exception",e.toString());
                        }catch (EOFException e) {
                            Log.e("exception",e.toString());
                        }catch (UnknownHostException e) {
                            Log.e("exception",e.toString());
                        } catch (IOException e) {
                            Log.e("exception",e.toString());
                        }catch(NullPointerException e){
                            Log.e("exceptionnull",e.toString());
                        } catch (Exception e) {
                            Log.e("exception",e.toString());
                        }
                    }

                }
                return matrixCursorstar;
            } else {
                if(recoverflag){try{Thread.sleep(2500);}catch(Exception e ){Log.e("sleepissue",e.toString());}}
                Log.i("insideno@*", selection);
                String portfoundquerside = "";
                for (String sl : Remote_PortsList) {
                    boolean found = checkparttitionQuery(selection, sl);
                    if (found) {
                        portfoundquerside = sl;
                        Log.i("itreachedhere", portfoundquerside);
                        break;
                    }
                }
                DHTclass first_succ = getPredecessorAndSuccessor(portfoundquerside);
                DHTclass secon_succ = getPredecessorAndSuccessor(first_succ.successor);
                String firstversion = "empty";
                String secondversion = "empty";
                String thirdversion = "empty";
                MatrixCursor matrixCursor2 = new MatrixCursor(new String[]{KEY_FIELD, VALUE_FIELD});
                String msgquery_zero = QUERYKEY + "##&" + portfoundquerside + "##&" + selection;
                AsyncTask<String, Void, String> query_zero = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgquery_zero, myclientport);
                try {
                    firstversion = query_zero.get();
                    Log.i(TAG, firstversion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                String msgquery_one = QUERYKEY + "##&" + first_succ.successor + "##&" + selection;
                AsyncTask<String, Void, String> query_one = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgquery_one, myclientport);
                try {
                    secondversion = query_one.get();
                    Log.i(TAG, firstversion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                String msgquery_two = QUERYKEY + "##&" + secon_succ.successor + "##&" + selection;
                AsyncTask<String, Void, String> query_two = new NodeClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msgquery_two, myclientport);
                try {
                    thirdversion = query_two.get();
                    Log.i(TAG, firstversion);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
                int a = 0;
                int b = 0;
                int c = 0;
                String max = "";
                int maxInt = 0;
                Log.i("versasg", firstversion + secondversion + thirdversion);
                if (firstversion != "empty" &&firstversion!="") {
                    a = Integer.parseInt(firstversion.split("##&")[1].split("_")[0]);
                    maxInt = a;
                }

                if (secondversion != "empty" &&secondversion!="") {
                    b = Integer.parseInt(secondversion.split("##&")[1].split("_")[0]);
                }
                if (a >= b && firstversion != "empty" &&firstversion!="") {
                    max = firstversion.split("##&")[1].split("_")[1];
                    maxInt = a;
                } else if (b >= a && secondversion != "empty"&&secondversion!="") {
                    max = secondversion.split("##&")[1].split("_")[1];
                }
                if (thirdversion != "empty"&&thirdversion!="") {
                    c = Integer.parseInt(thirdversion.split("##&")[1].split("_")[0]);
                    if (c > maxInt) {
                        max = thirdversion.split("##&")[1].split("_")[1];
                    }
                }
                Log.i("max", max);
                matrixCursor2.addRow(new Object[]{selection, max});
                return matrixCursor2;
            }

        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO Auto-generated method stub
        return 0;
    }

    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {
        @Override
        protected Void doInBackground(ServerSocket... serverSockets) {
            ServerSocket serverSocket = serverSockets[0];
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

                    if(messages[0].equals(INSERT)){
                        String valfield=messages[2];
                        Log.i("checkingversion",messages[2]);
                        FileInputStream inputStream2 ;
                        String valuein1="empty";
                        FileInputStream inputcheck;
                        try {
                            inputcheck = getContext().openFileInput(messages[1]);
                            BufferedReader bfrchck = new BufferedReader(new InputStreamReader(inputcheck));
                            valuein1 = bfrchck.readLine();
                            inputcheck.close();
                        }catch(Exception e){
                            Log.i("filenotfoud",e.toString());
                            //continue;
                        }
                        if(valuein1!="empty" && !messages[1].equals("123dummy123")){
                            String [] split=valuein1.split("_");
                            int ver=Integer.parseInt(split[0])+1;
                            String[] test=valfield.split("_");
                            valfield=Integer.toString(ver)+"_"+test[1];
                        }
                        FileOutputStream outputStream;
                        try {
                            outputStream = getContext().openFileOutput(messages[1], Context.MODE_PRIVATE);
                            outputStream.write(valfield.getBytes());
                            outputStream.close();
                        } catch (Exception e) {
                            Log.e("serverinsert", "File write failed");
                        }
                        //Thread.sleep();
                        DataOutputStream cl_pwstrf= new DataOutputStream(s.getOutputStream());
                        cl_pwstrf.writeUTF("AcknowledgeToclose");
                        cl_pwstrf.flush();


                    }
                    if(messages[0].equals(INITIALDELETEINSERT)){
                        String dummykey=messages[1];
                        FileInputStream inputStream1;
                        String valuein2="empty";
                        //String[] filesList = getContext().fileList();

                        try {
                            inputStream1 = getContext().openFileInput(dummykey);
                            BufferedReader bfrchckon1 = new BufferedReader(new InputStreamReader(inputStream1));
                            valuein2 = bfrchckon1.readLine();
                            inputStream1.close();
                        }catch(Exception e){
                            Log.i("filenotfoud",e.toString());
                            //continue;
                        }
                        if(valuein2!="empty"){
                            chekingdummy=true;
                        }
                        else{
                            FileOutputStream osd;
                            String ds="1_checkingdummyinsert";
                            try {
                                osd = getContext().openFileOutput("123dummy123", Context.MODE_PRIVATE);
                                osd.write(ds.getBytes());
                                osd.close();
                            } catch (Exception e) {
                                Log.e("serverinsert", "File write failed");
                            }
                        }
                        String[] filesList = getContext().fileList();
                        if(filesList.length>0) {
                            for (String st : filesList) {
                                if(!st.equals("123dummy123")){
                                    getContext().deleteFile(st);}
                            }
                        }

                        //Thread.sleep(2000);
                        DataOutputStream cl_pwstrs= new DataOutputStream(s.getOutputStream());
                        cl_pwstrs.writeUTF(String.valueOf(chekingdummy));
                        cl_pwstrs.flush();

                    }
                    else if(messages[0].equals(QUERY)){
                        String key = messages[1];
                        String stu="";
                        FileInputStream inputStreamq=getContext().openFileInput(key);
                        BufferedReader bfrdq = new BufferedReader(new InputStreamReader(inputStreamq));
                        String jesc = bfrdq.readLine();
                        stu=key+"##&"+jesc;
                        inputStreamq.close();
                        DataOutputStream cl_pw2q= new DataOutputStream(s.getOutputStream());
                        cl_pw2q.writeUTF(stu);
                        cl_pw2q.flush();
                    }
                    else  if(messages[0].equals(STARQUERY)) {
                        String[] filesList = getContext().fileList();
                        FileInputStream inputStreamstr;
                        String ret="SPEC";
                        for (String f : filesList) {
                            Log.i(TAG,f);
                            inputStreamstr = getContext().openFileInput(f);
                            BufferedReader bffrstr = new BufferedReader(new InputStreamReader(inputStreamstr));
                            String sdsa = bffrstr.readLine();
                            ret=ret+"##&"+f+"##"+sdsa;
                            inputStreamstr.close();
                        }
                        DataOutputStream cl_pwstr= new DataOutputStream(s.getOutputStream());
                        cl_pwstr.writeUTF(ret);
                        cl_pwstr.flush();
                    }
                    else if(messages[0].equals(DELETE)){
                        String key=messages[1];
                        getContext().deleteFile(key);
                        DataOutputStream cl_pwd1= new DataOutputStream(s.getOutputStream());
                        String std="DeleteAcknow";
                        cl_pwd1.writeUTF(std);
                        cl_pwd1.flush();
                    }
                    else if(messages[0].equals(PREDECESSORQUERY)){
                        Log.i("predece", "oe");
                        String[] filesListps = getContext().fileList();
                        FileInputStream inputStreamstrps;
                        String retps = "SPEC" + "##";
                        for (String f : filesListps) {
                            Log.i(TAG, f);
                            inputStreamstrps = getContext().openFileInput(f);
                            BufferedReader bffrstrps = new BufferedReader(new InputStreamReader(inputStreamstrps));
                            String sdsaps = bffrstrps.readLine();
                            if(!f.equals("123dummy123")){
                                boolean chd=checkparttitionQuery(f,messages[1]);
                                boolean chd2=checkparttitionQuery(f,messages[2]);
                                Log.i("checkingkey",chd+f);
                                if(chd||chd2) {
                                    retps = retps + f + "&&&" + sdsaps + "##";
                                }}
                            inputStreamstrps.close();}

                        Log.i("isithere", retps);
                        DataOutputStream cl_pwstrps = new DataOutputStream(s.getOutputStream());
                        cl_pwstrps.writeUTF(retps);
                        cl_pwstrps.flush();
                    }
                    else if(messages[0].equals(SUCCESSORQUERY)){
                        Log.i("predece", "oe");
                        String[] filesListps = getContext().fileList();
                        FileInputStream inputStreamstrps9;
                        String retps9 = "SPEC" + "##";
                        for (String f : filesListps) {
                            Log.i(TAG, f);
                            inputStreamstrps9 = getContext().openFileInput(f);
                            BufferedReader bffrstrps9 = new BufferedReader(new InputStreamReader(inputStreamstrps9));
                            String sdsaps = bffrstrps9.readLine();
                            if(!f.equals("123dummy123")) {
                                boolean chekpar2 = checkparttitionQuery(f, messages[2]);
                                if (chekpar2) {
                                    retps9 = retps9 + f + "&&&" + sdsaps + "##";
                                }}
                            inputStreamstrps9.close();

                        }
                        Log.i("isithere", retps9);
                        DataOutputStream cl_pwstrps9 = new DataOutputStream(s.getOutputStream());
                        cl_pwstrps9.writeUTF(retps9);
                        cl_pwstrps9.flush();
                    }
                    else if(messages[0].equals(ONCREATEINSERT)){
                        String[] splitsps = messages[1].split("##");
                        for (String sss : splitsps) {
                            if (!sss.equals("SPEC")&&!sss.equals("empty")) {
                                String[] thirdsplitsps = sss.split("&&&");
                                Log.i("splitkey", sss);
                                if(!thirdsplitsps[0].equals("123dummy123")) {
                                    FileOutputStream outputStreamspspon;
                                    try {
                                        outputStreamspspon = getContext().openFileOutput(thirdsplitsps[0], Context.MODE_PRIVATE);
                                        outputStreamspspon.write(thirdsplitsps[1].getBytes());
                                        Log.i("finishedwr", thirdsplitsps[0]);
                                        outputStreamspspon.close();
                                    } catch (Exception e) {
                                        Log.e("serverinsert", "File write failed");
                                    }
                                }

                            }
                        }
                        DataOutputStream cl_pwdsps = new DataOutputStream(s.getOutputStream());
                        String stdsp = "CloseAcknow";
                        cl_pwdsps.writeUTF(stdsp);
                        cl_pwdsps.flush();

                    }
                    else if(messages[0].equals(QUERYKEY)){

                        String keyquery = messages[1];
                        String stu="empty";
                        try{
                            FileInputStream keyqueryinputsream=getContext().openFileInput(keyquery);
                            BufferedReader bfrdqqyet = new BufferedReader(new InputStreamReader(keyqueryinputsream));
                            String jescquet = bfrdqqyet.readLine();
                            keyqueryinputsream.close();
                            stu=keyquery+"##&"+jescquet;
                        }
                        catch(Exception e)
                        {
                            //stu="empty";
                            Log.e("keyfff",e.toString());
                            //Log.i("querykey",stu);
                        }

                        DataOutputStream cl_pw2quetry= new DataOutputStream(s.getOutputStream());
                        cl_pw2quetry.writeUTF(stu);
                        cl_pw2quetry.flush();
                    }
                }
            }
            catch(NullPointerException e){
                Log.e("exception",e.toString());
            } catch (Exception e) {
                Log.e(TAG, e.toString());
            }
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
    DHTclass getPredecessorAndSuccessor(String idport) {
        for (DHTclass ob: ringlist){
            if(Integer.parseInt(idport)==Integer.parseInt(ob.id)){
                return ob;
            }
        }
        return null;
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
    String getHashValue(String hasvale){
        String hashvalue="";
        try {
            hashvalue=genHash(hasvale);
        } catch (NoSuchAlgorithmException e) {
            Log.e("InHashfunctionge",e.toString());
        }
        return hashvalue;
    }
    void UpdatePredecessorAndSuccessor(ArrayList<DHTclass> listtoUpdate) {
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
    private String genHash(String input) throws NoSuchAlgorithmException {
        MessageDigest sha1 = MessageDigest.getInstance("SHA-1");
        byte[] sha1Hash = sha1.digest(input.getBytes());
        Formatter formatter = new Formatter();
        for (byte b : sha1Hash) {
            formatter.format("%02x", b);
        }
        return formatter.toString();
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
            String returnstring="empty";
            Log.i("InsideCientSide",msgs[0]);
            String msgSend = msgs[0];
            String []msgToSend = msgSend.split("##&");
            if(msgToSend[0].equals(INSERT)){
                try {
                    Socket socket1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));
                    String [] keyval=msgToSend[2].split("##");
                    String dt=INSERT+"##&"+keyval[0]+"##&"+keyval[1];
                    Log.i("clientsidemess",dt);
                    DataOutputStream cl_pw1= new DataOutputStream(socket1.getOutputStream());
                    cl_pw1.writeUTF(dt);
                    cl_pw1.flush();
                    DataInputStream dip1= new DataInputStream(socket1.getInputStream());
                    String textreturnstring=dip1.readUTF();
                    Log.i("Insrt",textreturnstring);
                    if(textreturnstring.equals("AcknowledgeToclose")){

                        Thread.sleep(1000);
                        returnstring="success";
                        dip1.close();
                        socket1.close();


                    } }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }
            }
            else if(msgToSend[0].equals(INITIALDELETEINSERT)){
                try{
                    Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));

                    String dt=INITIALDELETEINSERT+"##&"+"123dummy123"+"##&"+"1_checkingdummyinsert";
                    Log.i("clientsidemess",dt);
                    DataOutputStream cl_pw2= new DataOutputStream(socket2.getOutputStream());
                    cl_pw2.writeUTF(dt);
                    cl_pw2.flush();
                    DataInputStream dip2= new DataInputStream(socket2.getInputStream());
                    String textreturnstring2=dip2.readUTF();
                    Log.i("Insrt",textreturnstring2);
                    Thread.sleep(500);
                    returnstring=textreturnstring2;
                    socket2.close();
                }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }
            }
            else if(msgToSend[0].equals(PREDECESSORQUERY)){
                try{
                    Socket socket2 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));
                    socket2.setSoTimeout(2000);
                    String dtp=PREDECESSORQUERY+"##&"+msgToSend[1]+"##&"+msgToSend[2];
                    Log.i("clientsidemess",dtp);
                    DataOutputStream cl_pwp= new DataOutputStream(socket2.getOutputStream());
                    cl_pwp.writeUTF(dtp);
                    cl_pwp.flush();
                    DataInputStream ds = new DataInputStream(socket2.getInputStream());
                    String anmsg=ds.readUTF();
                    returnstring=anmsg;
                    Thread.sleep(1000);
                    socket2.close();
                }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }

            }
            else if(msgToSend[0].equals(SUCCESSORQUERY)){
                try{
                    Socket socket9 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));
                    socket9.setSoTimeout(2000);
                    String dtp9=SUCCESSORQUERY+"##&"+msgToSend[1]+"##&"+msgToSend[2];
                    Log.i("clientsidemess",dtp9);
                    DataOutputStream cl_pwp9= new DataOutputStream(socket9.getOutputStream());
                    cl_pwp9.writeUTF(dtp9);
                    cl_pwp9.flush();
                    DataInputStream ds = new DataInputStream(socket9.getInputStream());
                    String anmsg=ds.readUTF();
                    returnstring=anmsg;
                    Thread.sleep(1000);
                    ds.close();
                    socket9.close();
                }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }

            }
            else if(msgToSend[0].equals(ONCREATEINSERT)){
                try{
                    Socket socket3 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));
                    socket3.setSoTimeout(2000);
                    String dtp2=ONCREATEINSERT+"##&"+msgToSend[2];
                    Log.i("clientsidemess",dtp2);
                    DataOutputStream cl_pwp3= new DataOutputStream(socket3.getOutputStream());
                    cl_pwp3.writeUTF(dtp2);
                    cl_pwp3.flush();
                    DataInputStream ds = new DataInputStream(socket3.getInputStream());
                    String anmsg=ds.readUTF();

                    returnstring="success";

                    if(anmsg.equals("CloseAcknow") ){
                        Log.i("gotack",anmsg);

                        Thread.sleep(1000);
                        ds.close();
                        socket3.close();}

                }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }

            }
            else if(msgToSend[0].equals(QUERYKEY)){
                try{
                    Socket socketquery1 = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                            Integer.parseInt(msgToSend[1]));
                    socketquery1.setSoTimeout(2000);
                    String dtp2=QUERYKEY+"##&"+msgToSend[2];
                    Log.i("clientsidemess",dtp2);
                    DataOutputStream cl_pwpquery3= new DataOutputStream(socketquery1.getOutputStream());
                    cl_pwpquery3.writeUTF(dtp2);
                    cl_pwpquery3.flush();
                    DataInputStream dsquery1 = new DataInputStream(socketquery1.getInputStream());
                    String anmsg=dsquery1.readUTF();
                    if(anmsg!=null&&anmsg!="") {
                        returnstring = anmsg;
                    }
                    else
                        returnstring="empty";
                    Log.i("returnquerykey",returnstring);
                    Log.i("gotack",anmsg);
                    Thread.sleep(500);
                    dsquery1.close();
                    socketquery1.close();

                }catch (SocketTimeoutException st) {
                    Log.e("exception",st.toString());
                } catch (StreamCorruptedException e) {
                    Log.e("exception",e.toString());
                } catch (FileNotFoundException e) {
                    Log.e("exception",e.toString());
                }catch (EOFException e) {
                    Log.e("exception",e.toString());
                }catch (UnknownHostException e) {
                    Log.e("exception",e.toString());
                } catch (IOException e) {
                    Log.e("exception",e.toString());
                }catch(NullPointerException e){
                    Log.e("exceptionnull",e.toString());
                } catch (Exception e) {
                    Log.e("exception",e.toString());
                }
            }
            return returnstring;
        }
    }
}
