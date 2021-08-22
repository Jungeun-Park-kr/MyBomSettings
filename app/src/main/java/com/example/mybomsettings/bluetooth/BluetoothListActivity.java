package com.example.mybomsettings.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.airbnb.lottie.LottieDrawable;
import com.example.mybomsettings.R;
import com.google.android.material.switchmaterial.SwitchMaterial;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;



@SuppressLint("LongLogTag")
public class BluetoothListActivity extends AppCompatActivity {
    /**
     * 블루투스 설정 메인 액티비티입니다.
     * - 블루투스 ON/OFF
     * - 저장된(페어링된) 블루투스 관리
     * - 새로운 블루투스 연결이 가능합니다.
     */
    private static Context baseContext;
    private static final String TAG = "BluetoothListActivity MyTag";
    public static String pairedDeviceState;

    // GATT 연결
    public static BluetoothGatt bluetoothGatt;
    public static boolean connected;

    // 블루투스
    private static BluetoothManager bluetoothManager ;
    private static BluetoothAdapter bluetoothAdapter;
    BluetoothDevice device;
    BluetoothDevice paired;
    public static BluetoothDevice connectedDevice;

    private static BluetoothSocket bluetoothSocket;
    static Handler bluetoothHandler;
    ConnectedThread threadConnectedBluetooth;

    // streams // 모든 연결 해제하기 위해 필요
    static InputStream           mBTInputStream  = null;
    static OutputStream          mBTOutputStream = null;

    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //  //fa87c0d0-afac-11de-8a39-0800200c9a66 (일반적인 Android 기기에서 대다수 사용하는 UUID)

    private static int mState;
    // 상태를 나타내는 상태 변수
    protected static final int STATE_NONE = 0; // we're doing nothing
    protected static final int STATE_LISTEN = 1; // now listening for incoming
    // connections
    protected static final int STATE_CONNECTING = 2; // now initiating an outgoing
    // connection
    protected static final int STATE_CONNECTED = 3; // now connected to a remote
    protected static final int STATE_DISCONNECTING = 4; // now connected to a remote
    // device

    // 블루투스 검색
    private boolean mScanning;
    private Handler handler;
    private static final long SCAN_PERIOD = 10000; // Stops scanning after 10 seconds.

    //Adapter
    private static BluetoothRAdapter pairedBluetoothRAdapter; // 페어링된(저장된) 블루투스 RecyclerView Adapter
    private SimpleAdapter availableBluetoothSAdapter; // 검색된 블루투스 Simple Adapter

    //list - Bluetooth 목록 저장
    static List<Bluetooth> pairedDevices; // 페어링된 디바이스 정보 저장 (pairedBluetoothRAdapter에 사용)
    private List<Map<String, String>> availableDevices; // 사용 가능한 디바이스 저장 (availableBluetoothSAdapter 에 사용)
    private List<BluetoothDevice> bluetoothDevices; // 사용 가능한 디바이스 저장 (실제로 연결하기 위해 사용)
    /**
     * <참고용>
     * availableDevices와 bluetoothDevices차이점
     * - availableDevices : availableBluetoothSAdapter에 넣을 디바이스 이름 넣기용 리스트
     * - bluetoothDevices : 사용가능한 블루투스 디바이스 객체를 담음 -> 연결에 이용하기 위한 리스트
     */

    private int selectDevice;

    
    // UI
    SwitchMaterial bluetoothSwitch; // 블루투스 사용 유무 스위치
    ListView availableListView; // 연결 가능한 디바이스
    RecyclerView pairedRecyclerView; // 등록된 디바이스를 위한 pairedRecyclerView

    TextView infoTextView; // 기기이름 및 연결 안내
    TextView errorTextView; // 에러 메시지
    static TextView infoMessage;
    Button searchBtn; // 블루투스 검색 버튼
    LinearLayout registeredLayout; // 등록된 디바이스 레이아웃
    LinearLayout availableLayout; // 사용 가능 디바이스 레이아웃
    public static LottieAnimationView lottieAnimationView; //(로딩모양) 검색중 로띠
    private static BluetoothGattServerCallback bluetoothGattServerCallback;
    private static BluetoothGattServer bluetoothGattServer;
    private static BluetoothGattCallback bluetoothGattCallback;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth_list);

        baseContext = getApplicationContext();

        // Use this check to determine whether BLE is supported on the device. Then
        // you can selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(this.getPackageManager().FEATURE_BLUETOOTH_LE)) {
            Log.e(TAG,"ble_not_supported");
            finish();
        }



        //블루투스 브로드캐스트 리시버 등록
        IntentFilter searchFilter = new IntentFilter();
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED); //BluetoothRAdapter.ACTION_DISCOVERY_STARTED : 블루투스 검색 시작
        searchFilter.addAction(BluetoothDevice.ACTION_FOUND); //BluetoothDevice.ACTION_FOUND : 블루투스 디바이스 찾음
        searchFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //BluetoothRAdapter.ACTION_DISCOVERY_FINISHED : 블루투스 검색 종료
        searchFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_CONNECTED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED);
        searchFilter.addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED);
        registerReceiver(bluetoothSearchReceiver, searchFilter);

        initializeAll();

        bluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == 1){
                    Log.e("connect success", "연결 성공");
                }
            }
        };

        bluetoothSwitch.setOnCheckedChangeListener(new bluetoothSwitchListener()); // 블루투스 ON/OFF 스위치 리스너

        searchBtn.setOnClickListener(l -> { // 블루투스 검색 버튼 리스너
            OnBluetoothSearch();
        });



        //검색된 디바이스목록 클릭시 페어링 요청
        availableListView.setOnItemClickListener((parent, view, position, id) -> {
            device = bluetoothDevices.get(position);
            try {
                Method method = device.getClass().getMethod("createBond", (Class[]) null);
                method.invoke(device, (Object[]) null);

                selectDevice = position;

                Log.e(TAG, "device: "+ String.valueOf(device));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    // 블루투스 ON/OFF 스위치 리스너
    class bluetoothSwitchListener implements CompoundButton.OnCheckedChangeListener{
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if(isChecked && (bluetoothAdapter == null || !bluetoothAdapter.isEnabled())) { // 스위치 ON
                bluetoothAdapter.enable();
                bluetoothAdapter = bluetoothManager.getAdapter();
                errorTextView.setVisibility(View.GONE);
                infoTextView.setText("블루투스가 켜져있는 동안 "+bluetoothAdapter.getName()+"이(가) 주변의 기기에 표시됩니다.");
                infoTextView.setVisibility(View.VISIBLE);
                pairedRecyclerView.setVisibility(View.VISIBLE);
                availableListView.setVisibility(View.VISIBLE);
                availableLayout.setVisibility(View.VISIBLE);
                registeredLayout.setVisibility(View.VISIBLE);
            }
            else { // 스위치 OFF
                bluetoothAdapter.disable();
                errorTextView.setVisibility(View.VISIBLE);
                infoTextView.setVisibility(View.GONE);
                pairedRecyclerView.setVisibility(View.GONE);
                availableListView.setVisibility(View.GONE);
                availableLayout.setVisibility(View.GONE);
                registeredLayout.setVisibility(View.GONE);
            }
        }
    }

    // Bluetooth 상태 set
    private static synchronized void setState(int state) {
        Log.d(TAG, "setState() " + mState + " -> " + state+", (0:연결X, 1:기다리는중 2:연결중, 3:연결됨");
        mState = state;
    }

    // Bluetooth 상태 get
    public static synchronized int getState() {
        return mState;
    }

    // 연결 실패했을때
    private void connectionFailed() {
        setState(STATE_LISTEN);
    }

    // 연결을 잃었을 때
    private static void connectionLost() {
        setState(STATE_LISTEN);

    }


    private void initializeAll() {
        // Initializes Bluetooth adapter.
        if (bluetoothAdapter == null && bluetoothManager == null) {
            bluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
            bluetoothAdapter = bluetoothManager.getAdapter();
        }


        infoMessage = findViewById(R.id.pairedNoInfo);

        bluetoothSwitch = (SwitchMaterial)findViewById(R.id.switch_bluetooth);
        availableListView = findViewById(R.id.list_available_devices);
        pairedRecyclerView = (RecyclerView)findViewById(R.id.recyclerview_registered_devices);
        searchBtn = findViewById(R.id.btn_bluetooth_search);
        infoTextView = findViewById(R.id.tv_bluetooth_info);
        errorTextView = findViewById(R.id.tv_bluetooth_error);
        registeredLayout = findViewById(R.id.layout_registered_bluetooth);
        availableLayout = findViewById(R.id.layout_available_bluetooth);
        lottieAnimationView = findViewById(R.id.lottie_loading);

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) { // 블루투스 꺼져 있는 경우
            bluetoothSwitch.setChecked(false);
            errorTextView.setVisibility(View.VISIBLE);
            infoTextView.setVisibility(View.GONE);
            pairedRecyclerView.setVisibility(View.GONE);
            availableListView.setVisibility(View.GONE);
            availableLayout.setVisibility(View.GONE);
            registeredLayout.setVisibility(View.GONE);
            Log.i(TAG,"현재 블루투스 꺼져 있음");
            return ;
        } else { // 블루투스 켜져 있는 경우
            bluetoothSwitch.setChecked(true);
            errorTextView.setVisibility(View.GONE);
            infoTextView.setText("블루투스가 켜져있는 동안 "+bluetoothAdapter.getName()+"이(가) 주변의 기기에 표시됩니다.");
            infoTextView.setVisibility(View.VISIBLE);
            pairedRecyclerView.setVisibility(View.VISIBLE);
            availableListView.setVisibility(View.VISIBLE);
            availableLayout.setVisibility(View.VISIBLE);
            registeredLayout.setVisibility(View.VISIBLE);
        }

        //검색된 블루투스 디바이스 데이터
        bluetoothDevices = new ArrayList<>();
        //선택한 디바이스 없음
        selectDevice = -1;

        // 페어링된 디바이스 - RecyclerView Adapter 사용
        setPairedDevices(false);

        // 사용 가능한 기기 - simpleAdapter 사용
        availableDevices = new ArrayList<>();
        availableBluetoothSAdapter = new SimpleAdapter(this, availableDevices, R.layout.item_bluetooth_list, new String[]{"name"}, new int[]{R.id.tv_bluetooth});
        availableListView.setAdapter(availableBluetoothSAdapter);
        
    }

    /**
     * 페어링된 디바이스 목록을 갱신하는 함수
     * paredDevices 리스트를 새로 생성해야 갱신 됨 (하나만 수정하거나 추가하면 반영이 안됨 -> 전체를 갱신하는 함수를 만듦)
     * @param isNecessary pairedDevices 리스트 필수 갱신 여부
     */
    public void setPairedDevices(boolean isNecessary) {
        if (pairedDevices == null || isNecessary) {
            if (bluetoothAdapter == null || bluetoothManager == null) {
                bluetoothManager = (BluetoothManager) getSystemService(this.BLUETOOTH_SERVICE);
                bluetoothAdapter = bluetoothManager.getAdapter();
            }
            // 페어링된 디바이스 목록 가져오기
            List<BluetoothDevice> tmpList = new ArrayList<>(bluetoothAdapter.getBondedDevices());
            pairedDevices = new ArrayList<>();
            for (BluetoothDevice device : tmpList) {
                // 연결된 디바이스 상태를 페어링된 디바이스 목록에 저장
                Bluetooth bluetooth = new Bluetooth(device);
                pairedDevices.add(bluetooth);
            }
        }

        pairedBluetoothRAdapter = new BluetoothRAdapter(this, pairedDevices);
        pairedBluetoothRAdapter.setHasStableIds(true); // 안깜빡임 (근데 깜빡임..ㅋ)
        pairedRecyclerView.setAdapter(pairedBluetoothRAdapter);

        if (pairedBluetoothRAdapter.getItemCount() == 0) { // 페어링 된 디바이스 없는 경우
            infoMessage.setVisibility(View.VISIBLE);
        } else {
            infoMessage.setVisibility(View.GONE);
        }
    }

    //블루투스 검색 버튼 클릭*****************************
    public void OnBluetoothSearch(){
        //검색버튼 비활성화
        searchBtn.setEnabled(false);
        if(bluetoothAdapter.isDiscovering()){ //블루투스 검색중인지 여부 확인
            bluetoothAdapter.cancelDiscovery(); //블루투스 검색 취소
        }

        bluetoothAdapter.startDiscovery(); //  블루투스 검색 시작
    }

    // 페어링 된 블루투스 연결
    public void pairSelectedDevice(BluetoothDevice selectedDevice) {
        Log.i(TAG,"Begin connect");
        bluetoothSocket = null;
        paired = null;

        bluetoothAdapter.cancelDiscovery(); // 연결 시도 전에는 기기 검색 중지 (연결 속도 느려지거나 실패할 수 있기 때문)
        paired = selectedDevice; // 새로 페어링 할 기기 정보

        try { // 선택한 디바이스 정보를 얻어 Bluetooth Sockect 생성
            bluetoothSocket = paired.createInsecureRfcommSocketToServiceRecord(MY_UUID);

        } catch (IOException e) {
            Log.e(TAG, "create() failed", e);
        }
        try { // BluetoothSocket 연결 시도
            bluetoothSocket.connect();
        } catch (IOException e) {
            if(paired.getBondState() == BluetoothDevice.BOND_BONDED) { // 오류 나도 페어링은 잘 됨...
                Log.i(TAG, "페어링은 또 되었다");
            } else { // 페어링도 안된 경우
                connectionFailed();
                Log.e(TAG, "블루투스 연결 중 오류가 발생했습니다. connect() 에러" + e);

                //Socket 닫기
                try {
                    bluetoothSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() socket during connection failure", e2);
                }
                return;
            }
        }
        Log.d(TAG, "connect() : Connect Succeeded");

        if (threadConnectedBluetooth != null) { // Cancel any thread currently running a connection
            threadConnectedBluetooth.cancel();
            threadConnectedBluetooth = null;
        }
        threadConnectedBluetooth = new ConnectedThread(bluetoothSocket);
        threadConnectedBluetooth.start();
        setState(STATE_CONNECTING);
        bluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();

        Log.e(TAG, "click4"+ String.valueOf(bluetoothSocket));
    }

    /**
     * Reset input and output streams and make sure socket is closed.
     * This method will be used during shutdown() to ensure that the connection is properly closed during a shutdown.
     * @return
     */
    private static void resetConnection() {
        if (mBTInputStream != null) {
            try {mBTInputStream.close();} catch (Exception e) {}
            mBTInputStream = null;
        }

        if (mBTOutputStream != null) {
            try {mBTOutputStream.close();} catch (Exception e) {}
            mBTOutputStream = null;
        }

        if (bluetoothSocket != null) {
            try {bluetoothSocket.close();} catch (Exception e) {}
            bluetoothSocket = null;
        }

    }

    public static void unpairDevice(BluetoothDevice device) {
        try {
            Method m = device.getClass()
                    .getMethod("removeBond", (Class[]) null);
            m.invoke(device, (Object[]) null);
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
        }

        for (Bluetooth bluetooth : pairedDevices) {
            if (bluetooth.getAddress().equals(device.getAddress())) {
                pairedDevices.remove(bluetooth); // 등록된 디바이스 목록에서 삭제
                break;
            }
        }
        Log.i(TAG, "List 변경됨 - pairedDevices의 길이:"+pairedDevices.size());
        updateBluetoothList(pairedDevices);
        pairedBluetoothRAdapter.notifyDataSetChanged();
        Log.i(TAG, "완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());

        if (pairedBluetoothRAdapter.getItemCount() == 0) { // 페어링 된 디바이스 없는 경우
            infoMessage.setVisibility(View.VISIBLE);
        } else {
            infoMessage.setVisibility(View.GONE);
        }
        Log.i(TAG, "갱신해서 삭제 완료");
    }

    static ConnectThread mConnectThread;
    static ConnectedThread mConnectedThread;

    public static void connectPairedDevice(BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        //setState(STATE_CONNECTING);
    }

    public static void disconnectPairedDevice(BluetoothDevice device) {
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
            Log.i(TAG, "connectThread.cancel()");
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
            Log.i(TAG, "connectedThread.cancel()");
        }
        resetConnection();
        //setState(STATE_NONE);
    }

    private static class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        /*private InputStream mmInput;
        private OutputStream mmOutput;*/

        public ConnectThread(BluetoothDevice device) {
            // Use a temporary object that is later assigned to mmSocket
            // because mmSocket is final.
            BluetoothSocket tmp = null;
            mmDevice = device;

            try {
                // Get a BluetoothSocket to connect with the given BluetoothDevice.
                // MY_UUID is the app's UUID string, also used in the server code.
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket's create() method failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            // Cancel discovery because it otherwise slows down the connection.
            setName("Connect Thread");
            bluetoothAdapter.cancelDiscovery();
            try {
                // Connect to the remote device through the socket. This call blocks
                // until it succeeds or throws an exception.
                mmSocket.connect();
            } catch (IOException connectException) {
                // Unable to connect; close the socket and return.
                try {
                    mmSocket.close();
                } catch (IOException closeException) {
                    Log.e(TAG, "Could not close the client socket", closeException);
                }
                return;
            }
            // Start the connected thread
            connected(mmSocket, mmDevice);

            /*// Do work to manage the connection (in a separate thread)
            manageConnectedSocket(mmSocket);*/
        }

        // Closes the client socket and causes the thread to finish.
        public void cancel() {
            if (mBTInputStream != null) {
                try {mBTInputStream.close();} catch (Exception e) {}
                mBTInputStream = null;
            }

            if (mBTOutputStream != null) {
                try {mBTOutputStream.close();} catch (Exception e) {}
                mBTOutputStream = null;
            }
            try {
                Thread.sleep(1000);
                mmSocket.close();
            } catch (IOException | InterruptedException e) {
                Log.e(TAG, "Could not close the client socket", e);
            }
        }
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public static synchronized void connected(BluetoothSocket socket, BluetoothDevice device) {
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }
        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }
        /*// Cancel the accept thread because we only want to connect to one device
        if (mAcceptThread != null) {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }*/
        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket);
        mConnectedThread.start();
        // Send the name of the connected device back to the UI Activity
        setState(STATE_CONNECTED);
    }


    private static class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {
            Log.d(TAG, "create ConnectedThread");
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // BluetoothSocket의 inputstream 과 outputstream을 얻는다.
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Log.e(TAG, "소켓 연결 중 오류가 발생했습니다.");
            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
            Log.e("bluetooth", "연결");

        }
        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) { // Keep listening to the InputStream while connected
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        bluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                        Log.e("bluetooth2", "연결");

                    }
                } catch (IOException e) {
                    Log.e(TAG, "disconnected", e);
                    connectionLost();
                    break;
                }
            }
        }
        /**
         * Write to the connected OutStream.
         *
         * @param buffer The bytes to write
         */
        public void write(byte[] buffer) {
            try {
                mmOutStream.write(buffer);
                // Share the sent message back to the UI Activity

            } catch (IOException e) {
            }
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
                Log.e(TAG,"소켓 해제 중 오류가 발생했습니다.");
            }
        }
    }


    //블루투스 검색결과
    final BroadcastReceiver bluetoothSearchReceiver = new BroadcastReceiver() {
        @RequiresApi(api = Build.VERSION_CODES.N)
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) { //블루투스 디바이스 검색 시작
                // 애니메이션
                lottieAnimationView.setVisibility(View.VISIBLE);
                setUpAnimation(lottieAnimationView);

                availableDevices.clear();
                bluetoothDevices.clear();
//                Log.e(TAG, "search start : " + "검색 시작");
                //로띠시작---아래에서
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) { //블루투스 디바이스 찾음
                //검색한 블루투스 디바이스의 객체를 구한다
                // Discovery has found a device. Get the BluetoothDevice
                device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "search name : " + device.getName() + ", search mac : " + device.getAddress());

                // 이미 검색된 디바이스인지 확인
                Optional result = availableDevices.stream().filter(x -> x.get("mac").equals(device.getAddress())).findFirst();
                if (result.isPresent()) {
                    Log.i(TAG, "찾음 : " + result);
                    System.out.println(result.get());
                } else {
                    System.out.println("데이터 없습니다 => " + device.getName());
                    //데이터 저장
                    Map map = new HashMap();
                    if (device.getName() == null)
                        map.put("name", device.getAddress());
                    else
                        map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                    map.put("mac", device.getAddress()); // 블루투스 디바이스 MAC 주소

                    /***** 블루투스 이름 필터링 - 나중에 필요하면 추가****
                     if(device.getName().substring(0,3).equals("GSM")){
                     map.put("name", device.getName()); //device.getName() : 블루투스 디바이스의 이름
                     Log.e("search name", device.getName());
                     //map.put("address", device.getAddress()); //device.getAddress() : 블루투스 디바이스의 MAC 주소
                     }*/

                    availableDevices.add(map);
                    //리스트 목록갱신
                    availableBluetoothSAdapter.notifyDataSetChanged();

                    //블루투스 디바이스 저장
                    bluetoothDevices.add(device);
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) { //블루투스 디바이스 검색 종료
                //로띠종료 검색버튼 활성화
                lottieAnimationView.setVisibility(View.INVISIBLE);
                searchBtn.setEnabled(true);
            } else if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) { //블루투스 디바이스 페어링 상태 변화
                paired = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (paired.getBondState() == BluetoothDevice.BOND_BONDED) {
                    //데이터 저장
                    pairSelectedDevice(paired);

                    // 완전 갱신..
                    setPairedDevices(true); // 무조건 갱신해야함
                    Log.e(TAG, "paired:" + String.valueOf(pairedDevices));

                    //검색된 목록
                    if (selectDevice != -1) {
                        bluetoothDevices.remove(selectDevice);

                        availableDevices.remove(selectDevice);
                        availableBluetoothSAdapter.notifyDataSetChanged();
                        selectDevice = -1;
                    }

                    if (pairedBluetoothRAdapter.getItemCount() == 0) { // 페어링 된 디바이스 없는 경우
                        infoMessage.setVisibility(View.VISIBLE);
                    } else {
                        infoMessage.setVisibility(View.GONE);
                    }

                    pairedBluetoothRAdapter.notifyDataSetChanged();

                } else if (paired.getBondState() == BluetoothDevice.BOND_NONE) { // 연결 해제
                   //Log.i(TAG, "연결 해제함");
                }
            }
            // 아래 브로드캐스트 처리는 BluetoothService.java에서 해줌
            else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) { // 연결됨
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Log.i(TAG, "연결된 애 있음. 갱신 시도..."+ connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(true);
                        updateBluetoothList(pairedDevices);
                        pairedBluetoothRAdapter.notifyDataSetChanged();
                        /*Log.i(TAG, "pairedDevices 리스트 완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결됨--" + connectedDevice.getName());*/
                            break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECT_REQUESTED.equals(action)) { // 연결해제 요청
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Log.i(TAG, "연결해제 될 애 있음. 갱신 시도..." + connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        pairedBluetoothRAdapter.notifyDataSetChanged();
                        /*Log.i(TAG, "pairedDevices 리스트 완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결 해제 요청--" + connectedDevice.getName());*/
                        break;
                    }
                }
            } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) { // 연결 해제됨
                connectedDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // Log.i(TAG, "연결해제 된 애 있음. 갱신 시도..." + connectedDevice.getName());
                for (Bluetooth bluetooth : pairedDevices) {
                    if (connectedDevice.getName().equals(bluetooth.getName().toString())) { // 일치하는 기기 찾기
                        pairedDevices.get(pairedDevices.indexOf(bluetooth)).setConnected(false);
                        updateBluetoothList(pairedDevices);
                        pairedBluetoothRAdapter.notifyDataSetChanged();
                        /*Log.i(TAG, "pairedDevices 리스트  완전 갱신함 - pairedDevices의 길이:"+pairedDevices.size());
                        Log.i(TAG, "--연결 해제됨--" + connectedDevice.getName());*/
                        break;
                    }
                }
            }
        }
    };



    public static void connectSelectedBLEDevice(BluetoothDevice device) {
        // Log.i(TAG, "선택한 기기 정보:"+device.toString());
        bluetoothGattCallback = new BluetoothGattCallback() {
            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
            }

            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status,
                                                int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    Log.i(TAG, "Connected to GATT server.");
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.i(TAG, "Disconnected from GATT server.");
                } else if (status == BluetoothProfile.STATE_CONNECTING) {
                    Log.i(TAG, "Connecting to GATT server.");
                } else if (status == BluetoothProfile.STATE_DISCONNECTING) {
                    Log.i(TAG, "DisConnecting from GATT server.");
                }
            }
        };

        // device가 직접 bluetoothGattServer가 되어 블루투스 연결
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
           bluetoothGatt = device.connectGatt(baseContext, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_AUTO);
        } else {
            bluetoothGatt = device.connectGatt(baseContext, false, bluetoothGattCallback);
        }
    }

    public static void disConnectBLEDevice(BluetoothDevice device) {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
        }
        if (bluetoothGattServer != null)
            bluetoothGattServer.cancelConnection(device);
    }

    public static void closeGatt() {
        if (bluetoothGatt == null) {
            return;
        } else if (bluetoothGattServer == null) {
            return;
        }
        bluetoothGatt.close();
        bluetoothGatt = null;
        bluetoothGattServer.close();
        bluetoothGattServer = null;
    }

    public static void updateBluetoothList(List<Bluetooth> newList) {
        List<Bluetooth> tmp = new ArrayList<>(newList);
        pairedDevices.clear();
        pairedDevices.addAll(tmp);
        if (bluetoothAdapter != null)
            pairedBluetoothRAdapter.notifyDataSetChanged();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(bluetoothSearchReceiver);
    }

    private void setUpAnimation(LottieAnimationView animview) { //로띠 애니메이션 설정
        //재생할 애니메이션
        animview.setAnimation("lottie_loading.json");
        //반복횟수 지정 : 무한
        animview.setRepeatCount(LottieDrawable.INFINITE); //아니면 횟수 지정
        //시작
        animview.playAnimation();
    }

}