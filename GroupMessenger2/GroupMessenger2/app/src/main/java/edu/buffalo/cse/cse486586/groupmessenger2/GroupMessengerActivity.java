package edu.buffalo.cse.cse486586.groupmessenger2;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StreamCorruptedException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * GroupMessengerActivity is the main Activity for the assignment.
 *
 * @author stevko
 *
 */
//References
//https://stackoverflow.com/questions/8304767/how-to-get-maximum-value-from-the-collection-for-example-arraylist
//
//https://stackoverflow.com/questions/13758640/how-should-i-iterate-a-priority-queue-properly
//
//https://stackoverflow.com/questions/12812307/how-to-remove-specific-value-from-string-array-in-java/12812355
public class GroupMessengerActivity extends Activity {
    static final String TAG = GroupMessengerActivity.class.getSimpleName();
    static final String REMOTE_PORT0 = "11108";
    static final String REMOTE_PORT1 = "11112";
    static final String REMOTE_PORT2 = "11116";
    static final String REMOTE_PORT3 = "11120";
    static final String REMOTE_PORT4 = "11124";
    static final int SERVER_PORT = 10000;

    public Uri mUri = buildUri("content", "edu.buffalo.cse.cse486586.groupmessenger2.provider");
    private static ContentValues contentValues = new ContentValues();

    ArrayList proposed_seq_list = new ArrayList();
    public int proposed_Seq_number=0;
    public  static double agreed_sequence_number=0;
    public int final_seq=0;
    private static final String KEY_FIELD = "key";
    private static final String VALUE_FIELD = "value";
    public  static String failedPort="0";
    public static String currentPort="";
    public static String clientport;
    PriorityQueue<MessageClass> messagesFromClient= new PriorityQueue<MessageClass>(800, new Comparator<MessageClass>() {
        @Override
        public int compare(MessageClass lhs, MessageClass rhs) {

            if (lhs.id < rhs.id)
                return -1;
            else if (lhs.id > rhs.id)
                return 1;
            return 0;}


    });
    //ArrayList<String> Remote_PortsList=new ArrayList<String>(Arrays.asList(REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4));
    public String [] Remote_PortsList= new String[]{REMOTE_PORT0,REMOTE_PORT1,REMOTE_PORT2,REMOTE_PORT3,REMOTE_PORT4};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group_messenger);
        TelephonyManager tel = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        String portStr = tel.getLine1Number().substring(tel.getLine1Number().length() - 4);
        final String myPort = String.valueOf((Integer.parseInt(portStr) * 2));
        clientport=myPort;
        /*
         * TODO: Use the TextView to display your messages. Though there is no grading component
         * on how you display the messages, if you implement it, it'll make your debugging easier.
         */
        try{
            ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
            new ServerTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, serverSocket);
        }
        catch(IOException e) {
            Log.e(TAG,e.toString());
            return;
        }
        TextView tv = (TextView) findViewById(R.id.textView1);
        tv.setMovementMethod(new ScrollingMovementMethod());
        final EditText editText =(EditText) findViewById(R.id.editText1);
        /*
         * Registers OnPTestClickListener for "button1" in the layout, which is the "PTest" button.
         * OnPTestClickListener demonstrates how to access a ContentProvider.
         */
        findViewById(R.id.button1).setOnClickListener(
                new OnPTestClickListener(tv, getContentResolver()));
        findViewById(R.id.button4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"click is working");
                String msg = editText.getText().toString();
                editText.setText("");
                new ClientTask().executeOnExecutor(AsyncTask.SERIAL_EXECUTOR, msg, myPort);
                //return true;
            }
        });
        /*
         * TODO: You need to register and implement an OnClickListener for the "Send" button.
         * In your implementation you need to get the message from the input box (EditText)
         * and send it to other AVDs.
         */
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_group_messenger, menu);
        return true;
    }
    private class ServerTask extends AsyncTask<ServerSocket, String, Void> {

        @Override
        protected Void doInBackground(ServerSocket... sockets) {
            ServerSocket serverSocket = sockets[0];
            //Log.i(TAG,"Inside doInBackground");
            try {
                String msgToRead="";
                Socket s;
                while(true) {
                    s = serverSocket.accept();
                    BufferedReader bfr = new BufferedReader(new InputStreamReader(s.getInputStream()));

                    Log.i("Before while", "YES");
                    msgToRead = bfr.readLine();
                    String[] stringsplit = msgToRead.split("@");
                    //currentPort = s.getPort();
                    boolean flag = Boolean.parseBoolean(stringsplit[3]);
                    if (!flag) {
                        //Log.i("float",String.valueOf(proposed_Seq_number));
                        PrintWriter pwr = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                        double id = Double.parseDouble(stringsplit[0]) + proposed_Seq_number;
                        proposed_Seq_number = proposed_Seq_number + 1;
                        messagesFromClient.add(new MessageClass(id, stringsplit[1], stringsplit[2], flag));
                        String msgToSendS = String.valueOf(id);
                        Log.i("Here in semdS", msgToSendS);
                        pwr.println(msgToSendS);
                        pwr.flush();
                    }
                    if (flag) {
                        PrintWriter pwr = new PrintWriter(new OutputStreamWriter(s.getOutputStream()), true);
                        Iterator<MessageClass> iter = messagesFromClient.iterator();
                        Log.i("Inside msgReadsever", stringsplit[0]);
                        while (iter.hasNext()) {
                            MessageClass obj = iter.next();
                            if (obj.message.equalsIgnoreCase(stringsplit[1])) {
                                messagesFromClient.remove(obj);
                                Log.i("Inside Updating flag", "Its here");
                                obj.id = Double.parseDouble(stringsplit[0]);
                                obj.insertflag = flag;
                                Log.i("Agregate", String.valueOf(obj.id));
                                messagesFromClient.add(obj);
                                proposed_Seq_number = Math.max((int) agreed_sequence_number, proposed_Seq_number) + 1;
                                Log.i("porp", String.valueOf(proposed_Seq_number));
                                break;
                            }
                        }
                        pwr.println("Acknowledgement");
                        pwr.flush();

                    }
                    Iterator<MessageClass> iter2 = messagesFromClient.iterator();

                     while (iter2.hasNext())
                     {
                         MessageClass i=iter2.next();
                         if(i.portnumber.equals(failedPort))
                         {
                             messagesFromClient.remove(i);
                         }
                     }

                    MessageClass msg=null;
                    while(((msg=messagesFromClient.peek())!=null))
                    {

                        if(!msg.portnumber.equals(failedPort) && msg.insertflag) {
                            //Log.i("Here in peek", String.valueOf(messagesFromClient.peek()));
                            Log.i("inside", "inside");
                            String msgFinal = msg.message;
                            Log.i("Inside remove", msgFinal);
                            publishProgress(msgFinal);
                            messagesFromClient.poll();
                        }
                        else
                        {
                            if(msg.portnumber.equals(failedPort) && !msg.insertflag)
                            {
                                messagesFromClient.poll();
                            }
                            else
                                break;
                        }
                        //}else
                        //{
                        //  if(msg.portnumber.equals(failedPort))
                        //{
                        //  messagesFromClient.poll();
                        //}
                        //else
                        //{
                        //  break;
                        //}
                    }

                    //}

                     s.close();
                }
            }



            catch(IOException e)
            {
                //failedPort=currentPort;
                Log.e("serversocketexception","serversocketexception");
            }

            catch(Exception e)
            {
                Log.e(TAG,e.toString());
            }

            /*
             * TODO: Fill in your server code that receives messages and passes them
             * to onProgressUpdate().
             */
            return null;
        }

        protected void onProgressUpdate(String...strings) {
            super.onProgressUpdate(strings);
            Log.i(TAG,"I am in OnProgress Update ");
            String strReceived =strings[0].trim() ;
            String keyreceived=Integer.toString(final_seq);
            contentValues.put(KEY_FIELD,keyreceived);
            contentValues.put(VALUE_FIELD,strReceived);
            getContentResolver().insert(mUri,contentValues);
            final_seq++;
            TextView remoteTextView = (TextView) findViewById(R.id.textView1);
            remoteTextView.append(strReceived + "\t\n");
            return;
        }


    }
    private class ClientTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... msgs) {
            try {

                for (String remotePort : Remote_PortsList) {
                    if (!remotePort.equals(failedPort)) {
                        Log.i("entered for remote port",remotePort);
                        String msgToSend = msgs[0];
                        currentPort = remotePort;
                        Socket socket = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort));
                        socket.setSoTimeout(2000);
                        Log.i("print value", String.valueOf("0." + remotePort + "1"));
                        String initial = "0." + remotePort;
                        String msg_cl = initial + "@" + msgToSend + "@" + clientport + "@" + Boolean.toString(false);
                        BufferedReader brd = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                        PrintWriter cl_pw = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);
                        Log.i("It atleast reached here", msg_cl);
                        cl_pw.println(msg_cl);
                        cl_pw.flush();
                        String initialMessage = null;
                        initialMessage = (String)brd.readLine();
                        Log.i(TAG, initialMessage);
                        Log.i(TAG, "I should come jhre e acatually ");
                        Log.i("CLIENTSUDE", initialMessage);
                        proposed_seq_list.add(Double.parseDouble(initialMessage));
                        Log.i(TAG, "THIS IS DONE");
                        //if(!socket.isClosed())
                        //{
                        //socket.close();}
                        //Thread.sleep(500);
                        socket.close();

                    }
                }
                agreed_sequence_number = (Double) Collections.max(proposed_seq_list);
                //}
            }
            // Thread.sleep(1000);

            catch (NullPointerException e) {
                failedPort = currentPort;
                //Log.i("failedport", failedPort);
                //Log.i("failedport",failedPort);
                Log.e("null should catch now", "it is time out exception");
            }
            catch (SocketTimeoutException e) {
                failedPort =currentPort;
                Log.i("failedport", failedPort);
                Log.e("timeoutexception", e.toString());
            } catch (StreamCorruptedException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("streamexception", e.toString());
            }
            catch (FileNotFoundException e) {
                failedPort=currentPort;
                Log.e(TAG, "File not found");
            }
            catch (EOFException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("eofexception", e.toString());
            } catch (IOException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("ioexception", e.toString());
            } catch (Exception e) {
                Log.e("crashed", "client crashed");
                Log.e(TAG, e.toString());
            }

            try {
                for (String remotePort : Remote_PortsList) {
                    if(!remotePort.equals(failedPort)) {
                        currentPort = remotePort;
                        Socket sock = new Socket(InetAddress.getByAddress(new byte[]{10, 0, 2, 2}),
                                Integer.parseInt(remotePort));
                        sock.setSoTimeout(2000);

                        BufferedReader brd2 = new BufferedReader(new InputStreamReader(sock.getInputStream()));
                        PrintWriter cl_pw2 = new PrintWriter(new OutputStreamWriter(sock.getOutputStream()), true);
                        Log.i("Is it working here", "here");
                        // proposed_seq_list.clear();
                        String sed = String.valueOf(agreed_sequence_number) + "@" + msgs[0] + "@" + clientport + "@" + Boolean.toString(true);
                        Log.i("jhjh", sed);
                        cl_pw2.println(sed);
                        cl_pw2.flush();
                        Log.i("sfter jh", sed);
                        String ackMsg = null;
                        ackMsg=(String)brd2.readLine();
                        if (ackMsg.equalsIgnoreCase("Acknowledgement")) {
                            sock.close();

                        }
                    }
                    // Thread.sleep(500);
                }
            }

            catch (NullPointerException e) {
                failedPort = currentPort;
                //Log.i("failedport", failedPort);
                //Log.i("failedport",failedPort);
                Log.e("null should catch now", "it is time out exception");
            }
            catch (SocketTimeoutException e) {
                failedPort =currentPort;
                Log.i("failedport", failedPort);
                Log.e("timeoutexception", e.toString());
            } catch (StreamCorruptedException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("streamexception", e.toString());
            }
            catch (FileNotFoundException e) {
                failedPort=currentPort;
                Log.e(TAG, "File not found");
            }
            catch (EOFException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("eofexception", e.toString());
            } catch (IOException e) {
                failedPort = currentPort;
                Log.i("failedport", failedPort);
                Log.e("ioexception", e.toString());
            } catch (Exception e) {
                Log.e("crashed", "client crashed");
                Log.e(TAG, e.toString());
            }

            return null;



        }}

    private class MessageClass {
        double id;
        String message;
        String portnumber;
        boolean insertflag;

        MessageClass(double id, String message, String portnumber, boolean insertflag) {

            this.id = id;
            this.message = message;
            this.portnumber = portnumber;
            this.insertflag = insertflag;
        }

        MessageClass() {
        }


    }
    private Uri buildUri(String scheme, String authority) {
        Uri.Builder uriBuilder = new Uri.Builder();
        uriBuilder.authority(authority);
        uriBuilder.scheme(scheme);
        return uriBuilder.build();
    }

}
