/**
 * Project Name:XPGSdkV4AppBase
 * File Name:MainControlActivity.java
 * Package Name:com.gizwits.aircondition.activity.control
 * Date:2015-1-27 14:44:17
 * Copyright (c) 2014~2015 Xtreme Programming Group, Inc.
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), 
 * to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, 
 * and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.gizwits.waterpurifier.activity.control;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.gizwits.framework.activity.BaseActivity;
import com.gizwits.framework.activity.account.UserManageActivity;
import com.gizwits.framework.activity.device.DeviceListActivity;
import com.gizwits.framework.activity.device.DeviceManageListActivity;
import com.gizwits.framework.activity.help.AboutActivity;
import com.gizwits.framework.activity.help.HelpActivity;
import com.gizwits.framework.adapter.MenuDeviceAdapter;
import com.gizwits.framework.config.JsonKeys;
import com.gizwits.framework.entity.DeviceAlarm;
import com.gizwits.framework.utils.DensityUtil;
import com.gizwits.framework.utils.DialogManager;
import com.gizwits.framework.widget.SlidingMenu;
import com.gizwits.framework.widget.SlidingMenu.SlidingMenuListener;
import com.gizwits.waterpurifier.R;
import com.xpg.common.system.IntentUtils;
import com.xpg.common.useful.DateUtil;
import com.xtremeprog.xpgconnect.XPGWifiDevice;

// TODO: Auto-generated Javadoc
/**
 * Created by Lien on 14/12/21.
 * 
 * 设备主控界面
 * 
 * @author Lien
 */
public class MainControlActivity extends BaseActivity implements
		OnClickListener, SlidingMenuListener {

	/** The tag. */
	private final String TAG = "MainControlActivity";

	/** The m view. */
	private SlidingMenu mView;

	/** The iv menu. */
	private ImageView ivMenu;
	
	/** The iv power. */
	private ImageView ivPower;
	
	//==================控制组件====================
	private ImageView pp_iv;
	private ImageView gac_iv;
	private ImageView cto_iv;
	private ImageView ro_iv;
	private ImageView tc_iv;
	private ImageView powerOffBtn;
	private ImageView cancel_btn;
	
	private TextView filter_tv;
	private TextView device_tv;
	private TextView type_tv;
	private TextView time_tv;
	private TextView life_tv;
	private TextView state_tv;
	
	private Button purifier_btn;
	private Button clean_btn;
	private Button reset_btn;

	private LinearLayout rlPowerOn;
	private RelativeLayout llFilterMsg;
	private LinearLayout llErrorMsgAlert;
	private LinearLayout llPowerOff;
	
	//=============================================
	
	//====================逻辑控制===================
	private int selectFilter = 0;
	private int[] FilterLifeList = new int[5];
	//=============================================

	/** The m adapter. */
	private MenuDeviceAdapter mAdapter;

	/** The lv device. */
	private ListView lvDevice;

	/** The device data map. */
	private ConcurrentHashMap<String, Object> deviceDataMap;

	/** The statu map. */
	private ConcurrentHashMap<String, Object> statuMap;

	/** The alarm list. */
	private ArrayList<DeviceAlarm> alarmList;

	/** The alarm list has shown. */
	private ArrayList<String> alarmShowList;

	/** The m fault dialog. */
	private Dialog mFaultDialog;

	/** The m PowerOff dialog. */
	private Dialog mPowerOffDialog;

	/** The progress dialog. */
	private ProgressDialog progressDialogRefreshing;

	/** The disconnect dialog. */
	private Dialog mDisconnectDialog;

	/** 是否超时标志位 */
	private boolean isTimeOut = false;

	/** 侧拉菜单 */
	private ScrollView slMenu;

	/** 获取状态超时时间 */
	private int GetStatueTimeOut = 30000;

	/** 登陆设备超时时间 */
	private int LoginTimeOut = 5000;

	/**
	 * ClassName: Enum handler_key. <br/>
	 * <br/>
	 * date: 2014-11-26 17:51:10 <br/>
	 * 
	 * @author Lien
	 */
	private enum handler_key {

		/** 更新UI界面 */
		UPDATE_UI,

		/** 显示警告 */
		ALARM,

		/** 设备断开连接 */
		DISCONNECTED,

		/** 接收到设备的数据 */
		RECEIVED,

		/** 获取设备状态 */
		GET_STATUE,

		/** 获取设备状态超时 */
		GET_STATUE_TIMEOUT,

		/** The login start. */
		LOGIN_START,

		/**
		 * The login success.
		 */
		LOGIN_SUCCESS,

		/**
		 * The login fail.
		 */
		LOGIN_FAIL,

		/**
		 * The login timeout.
		 */
		LOGIN_TIMEOUT,
	}

	/**
	 * The handler.
	 */
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			handler_key key = handler_key.values()[msg.what];
			switch (key) {
			case RECEIVED:
				try {
					if (deviceDataMap.get("data") != null) {
						Log.i("info", (String) deviceDataMap.get("data"));
						inputDataToMaps(statuMap,
								(String) deviceDataMap.get("data"));

					}
					alarmList.clear();
					if (deviceDataMap.get("alters") != null) {
						Log.i("info", "alerts"+(String) deviceDataMap.get("alters"));
						inputAlarmToList((String) deviceDataMap.get("alters"));
					}
					if (deviceDataMap.get("faults") != null) {
						Log.i("info", "faults"+(String) deviceDataMap.get("faults"));
						inputAlarmToList((String) deviceDataMap.get("faults"));
					}
					// 返回主线程处理P0数据刷新
					handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
					handler.sendEmptyMessage(handler_key.ALARM.ordinal());
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			case UPDATE_UI:
				if (mView.isOpen())
					break;

				if (statuMap != null && statuMap.size() > 0) {
					handler.removeMessages(handler_key.GET_STATUE_TIMEOUT
							.ordinal());
					
					updatePowerSwitch((Boolean) statuMap.get(JsonKeys.ON_OFF));
					FilterLifeList = new int[]{Integer.parseInt(statuMap.get(JsonKeys.Filter_1_Life).toString()),
							Integer.parseInt(statuMap.get(JsonKeys.Filter_2_Life).toString()),
							Integer.parseInt(statuMap.get(JsonKeys.Filter_3_Life).toString()),
							Integer.parseInt(statuMap.get(JsonKeys.Filter_4_Life).toString()),
							Integer.parseInt(statuMap.get(JsonKeys.Filter_5_Life).toString())};
					if (selectFilter != 0) {
						showFilterMsg();
					}
					if (Integer.parseInt(statuMap.get(JsonKeys.Mode).toString()) == 1) {
						setClean();
					}else if(Integer.parseInt(statuMap.get(JsonKeys.Mode).toString()) == 2){
						setFilterClean();
					}else{
						setNormal();
					}
					DialogManager.dismissDialog(MainControlActivity.this,
							progressDialogRefreshing);
				}
				break;
			case ALARM:
				if(alarmList.size() != 0){
					showErrorMsg(true);
				}else{
					showErrorMsg(false);
				}
				break;
			case DISCONNECTED:
				if (!mView.isOpen()) {
					DialogManager.dismissDialog(MainControlActivity.this,
							progressDialogRefreshing);
					DialogManager.dismissDialog(MainControlActivity.this,
							mFaultDialog);
					DialogManager.dismissDialog(MainControlActivity.this,
							mPowerOffDialog);
					DialogManager.showDialog(MainControlActivity.this,
							mDisconnectDialog);
				}
				break;
			case GET_STATUE:
				mCenter.cGetStatus(mXpgWifiDevice);
				break;
			case GET_STATUE_TIMEOUT:
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_SUCCESS:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				refreshMainControl();
				break;
			case LOGIN_FAIL:
				handler.removeMessages(handler_key.LOGIN_TIMEOUT.ordinal());
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			case LOGIN_TIMEOUT:
				isTimeOut = true;
				handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
				break;
			}
		}
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#onCreate(android.os.Bundle
	 * )
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main_control);
		initViews();
		initEvents();
		initParams();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.aircondition.activity.BaseActivity#onResume()
	 */
	@Override
	public void onResume() {
		if (mView.isOpen()) {
			refreshMenu();
		} else {
			if (!mDisconnectDialog.isShowing())
				refreshMainControl();
		}
		super.onResume();

	}

	/**
	 * 更新菜单界面.
	 * 
	 * @return void
	 */
	private void refreshMenu() {
		initBindList();
		mAdapter.setChoosedPos(-1);
		for (int i = 0; i < bindlist.size(); i++) {
			if (bindlist.get(i).getDid()
					.equalsIgnoreCase(mXpgWifiDevice.getDid()))
				mAdapter.setChoosedPos(i);
		}

		// 当前绑定列表没有当前操作设备
		if (mAdapter.getChoosedPos() == -1) {
			mAdapter.setChoosedPos(0);
			mXpgWifiDevice = mAdapter.getItem(0);
			alarmList.clear();
		}

		mAdapter.notifyDataSetChanged();

		int px = DensityUtil.dip2px(this, mAdapter.getCount() * 50);
		lvDevice.setLayoutParams(new android.widget.LinearLayout.LayoutParams(
				android.widget.LinearLayout.LayoutParams.WRAP_CONTENT, px));
	}

	/**
	 * 更新主控制界面.
	 * 
	 * @return void
	 */
	private void refreshMainControl() {
		mXpgWifiDevice.setListener(deviceListener);
		DialogManager.showDialog(this, progressDialogRefreshing);
		handler.sendEmptyMessageDelayed(
				handler_key.GET_STATUE_TIMEOUT.ordinal(), GetStatueTimeOut);
		handler.sendEmptyMessage(handler_key.GET_STATUE.ordinal());
	}

	/**
	 * Inits the params.
	 */
	private void initParams() {
		statuMap = new ConcurrentHashMap<String, Object>();
		alarmList = new ArrayList<DeviceAlarm>();
		alarmShowList = new ArrayList<String>();

		refreshMenu();
		refreshMainControl();
	}

	/**
	 * Inits the views.
	 */
	private void initViews() {
		mView = (SlidingMenu) findViewById(R.id.main_layout);
		ivMenu = (ImageView) findViewById(R.id.ivMenu);
		ivPower = (ImageView) findViewById(R.id.ivPower);
		
		//====================控制组件=========================
		pp_iv = (ImageView) findViewById(R.id.pp_iv);
		gac_iv = (ImageView) findViewById(R.id.gac_iv);
		cto_iv = (ImageView) findViewById(R.id.cto_iv);
		ro_iv = (ImageView) findViewById(R.id.ro_iv);
		tc_iv = (ImageView) findViewById(R.id.tc_iv);
		
		filter_tv = (TextView) findViewById(R.id.filter_tv);
		device_tv = (TextView) findViewById(R.id.device_tv);
		type_tv = (TextView) findViewById(R.id.type_tv);
		time_tv = (TextView) findViewById(R.id.time_tv);
		life_tv = (TextView) findViewById(R.id.life_tv);
		state_tv = (TextView) findViewById(R.id.state_tv);
		
		purifier_btn = (Button) findViewById(R.id.purifier_btn);
		clean_btn = (Button) findViewById(R.id.clean_btn);
		powerOffBtn = (ImageView) findViewById(R.id.powerOffBtn);
		reset_btn = (Button) findViewById(R.id.reset_btn);
		cancel_btn = (ImageView) findViewById(R.id.cancel_btn);

		rlPowerOn = (LinearLayout) findViewById(R.id.rlPowerOn);
		llFilterMsg = (RelativeLayout) findViewById(R.id.llFilterMsg);
		llFilterMsg.getBackground().setAlpha(188);
		llErrorMsgAlert = (LinearLayout) findViewById(R.id.llErrorMsgAlert);
		llErrorMsgAlert.getBackground().setAlpha(188);
		llPowerOff = (LinearLayout) findViewById(R.id.llPowerOff);
		//====================================================

		mPowerOffDialog = DialogManager.getPowerOffDialog(this,
				new OnClickListener() {

					@Override
					public void onClick(View arg0) {
						mCenter.cSwitchOn(mXpgWifiDevice, false);
						DialogManager.dismissDialog(MainControlActivity.this,
								mPowerOffDialog);
					}
				});
		mFaultDialog = DialogManager.getDeviceErrirDialog(
				MainControlActivity.this, "设备故障", new OnClickListener() {

					@Override
					public void onClick(View v) {
						Intent intent = new Intent(Intent.ACTION_CALL, Uri
								.parse("tel:10086"));
						startActivity(intent);
						mFaultDialog.dismiss();
					}
				});

		mAdapter = new MenuDeviceAdapter(this, bindlist);
		lvDevice = (ListView) findViewById(R.id.lvDevice);
		lvDevice.setAdapter(mAdapter);
		slMenu = (ScrollView) findViewById(R.id.slMenu);

		progressDialogRefreshing = new ProgressDialog(MainControlActivity.this);
		progressDialogRefreshing.setMessage("正在更新状态,请稍后。");
		progressDialogRefreshing.setCancelable(false);

		mDisconnectDialog = DialogManager.getDisconnectDialog(this,
				new OnClickListener() {
					@Override
					public void onClick(View v) {
						DialogManager.dismissDialog(MainControlActivity.this,
								mDisconnectDialog);
						IntentUtils.getInstance().startActivity(
								MainControlActivity.this,
								DeviceListActivity.class);
						finish();
					}
				});

	}

	/**
	 * Inits the events.
	 */
	private void initEvents() {
		ivMenu.setOnClickListener(this);
		ivPower.setOnClickListener(this);
		
		//===
		pp_iv.setOnClickListener(this);
		gac_iv.setOnClickListener(this);
		cto_iv.setOnClickListener(this);
		ro_iv.setOnClickListener(this);
		tc_iv.setOnClickListener(this);
		purifier_btn.setOnClickListener(this);
		clean_btn.setOnClickListener(this);
		powerOffBtn.setOnClickListener(this);
		reset_btn.setOnClickListener(this);
		cancel_btn.setOnClickListener(this);
		rlPowerOn.setOnClickListener(null);
		llFilterMsg.setOnClickListener(null);
		llErrorMsgAlert.setOnClickListener(null);
		llPowerOff.setOnClickListener(null);
		//===
		
		lvDevice.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				if (!mAdapter.getItem(position).isOnline())
					return;

				if (mAdapter.getChoosedPos() != position) {
					alarmShowList.clear();
					mAdapter.setChoosedPos(position);
					mXpgWifiDevice = bindlist.get(position);
				}

				mView.toggle();
			}
		});
		mView.setSlidingMenuListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.view.View.OnClickListener#onClick(android.view.View)
	 */
	@Override
	public void onClick(View v) {
		if (mView.isOpen()) {
			return;
		}

		switch (v.getId()) {
		case R.id.ivMenu:
			slMenu.scrollTo(0, 0);
			mView.toggle();
			break;
		case R.id.powerOffBtn:
			mCenter.cSwitchOn(mXpgWifiDevice, true);
			updatePowerSwitch(true);
			break;
		case R.id.clean_btn:
			setClean();
			mCenter.cSetMode(mXpgWifiDevice, 1);
			break;
		case R.id.purifier_btn:
			setFilterClean();
			mCenter.cSetMode(mXpgWifiDevice, 2);
			break;
		case R.id.reset_btn:
			switch (selectFilter) {
			case 1:
				mCenter.cSetLife1(mXpgWifiDevice, 2880);
				break;
			case 2:
				mCenter.cSetLife2(mXpgWifiDevice, 8640);
				break;
			case 3:
				mCenter.cSetLife3(mXpgWifiDevice, 8640);
				break;
			case 4:
				mCenter.cSetLife4(mXpgWifiDevice, 17280);
				break;
			case 5:
				mCenter.cSetLife5(mXpgWifiDevice, 8640);
				break;
			}
			break;
		case R.id.ivPower:
			mCenter.cSwitchOn(mXpgWifiDevice, false);
			updatePowerSwitch(false);
			break;
		case R.id.cancel_btn:
			selectFilter = 0;
			dismissFilterMsg();
			break;
		case R.id.pp_iv:
			selectFilter = 1;
			showFilterMsg();
			break;
		case R.id.gac_iv:
			selectFilter = 2;
			showFilterMsg();
			break;
		case R.id.cto_iv:
			selectFilter = 3;
			showFilterMsg();
			break;
		case R.id.ro_iv:
			selectFilter = 4;
			showFilterMsg();
			break;
		case R.id.tc_iv:
			selectFilter = 5;
			showFilterMsg();
			break;
			
		}
	}

	/**
	 * 菜单界面点击事件监听方法.
	 * 
	 * @return void
	 */
	public void onClickSlipBar(View view) {
		if (!mView.isOpen())
			return;

		switch (view.getId()) {
		case R.id.rlDevice:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					DeviceManageListActivity.class);
			break;
		case R.id.rlAbout:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					AboutActivity.class);
			break;
		case R.id.rlAccount:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					UserManageActivity.class);
			break;
		case R.id.rlHelp:
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					HelpActivity.class);
			break;
		case R.id.btnDeviceList:
			mCenter.cDisconnect(mXpgWifiDevice);
			DisconnectOtherDevice();
			IntentUtils.getInstance().startActivity(MainControlActivity.this,
					DeviceListActivity.class);
			finish();
			break;
		}
	}

	/**
	 * 菜单界面返回主控界面.
	 * 
	 * @return void
	 */
	private void backToMain() {
		mXpgWifiDevice = mAdapter.getItem(mAdapter.getChoosedPos());

		if (!mXpgWifiDevice.isConnected()) {
			loginDevice(mXpgWifiDevice);
			DialogManager.showDialog(this, progressDialogRefreshing);
		}

		refreshMainControl();
	}

	/**
	 * Login device.
	 * 
	 * @param xpgWifiDevice
	 *            the xpg wifi device
	 */
	private void loginDevice(XPGWifiDevice xpgWifiDevice) {
		mXpgWifiDevice = xpgWifiDevice;

		mXpgWifiDevice.setListener(deviceListener);
		mXpgWifiDevice.login(setmanager.getUid(), setmanager.getToken());
		isTimeOut = false;
		handler.sendEmptyMessageDelayed(handler_key.LOGIN_TIMEOUT.ordinal(),
				LoginTimeOut);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.gizwits.framework.activity.BaseActivity#didLogin(com.xtremeprog.
	 * xpgconnect.XPGWifiDevice, int)
	 */
	@Override
	protected void didLogin(XPGWifiDevice device, int result) {
		if (isTimeOut)
			return;

		if (result == 0) {
			handler.sendEmptyMessage(handler_key.LOGIN_SUCCESS.ordinal());
		} else {
			handler.sendEmptyMessage(handler_key.LOGIN_FAIL.ordinal());
		}

	}

	/**
	 * 检查出了选中device，其他device有没有连接上
	 * 
	 * @param mac
	 *            the mac
	 * @param did
	 *            the did
	 * @return the XPG wifi device
	 */
	private void DisconnectOtherDevice() {
		for (XPGWifiDevice theDevice : bindlist) {
			if (theDevice.isConnected()
					&& !theDevice.getDid().equalsIgnoreCase(
							mXpgWifiDevice.getDid()))
				mCenter.cDisconnect(theDevice);
		}
	}

	// ==================================================================================
	/**
	 * 更新开关状态.
	 * 
	 * @param isSwitch
	 *            the isSwitch
	 */
	private void updatePowerSwitch(boolean isSwitch) {
		if (isSwitch) {// 开机
			llPowerOff.setVisibility(View.GONE);
			rlPowerOn.setVisibility(View.VISIBLE);
			ivPower.setVisibility(View.VISIBLE);
		} else {// 关机
			llPowerOff.setVisibility(View.VISIBLE);
			rlPowerOn.setVisibility(View.GONE);
			ivPower.setVisibility(View.GONE);
		}
	}

	private void showErrorMsg(boolean haveError){
		if(haveError){
			llErrorMsgAlert.setVisibility(View.VISIBLE);
			ivPower.setVisibility(View.GONE);
		}else{
			llErrorMsgAlert.setVisibility(View.GONE);
			ivPower.setVisibility(View.VISIBLE);
		}
	}
	
	private void showFilterMsg(){
		llFilterMsg.setVisibility(View.VISIBLE);
		switch (selectFilter) {
		case 1:
			type_tv.setText("类型：PP棉");
			time_tv.setText("时间："+(FilterLifeList[0])+"小时");
			break;
		case 2:
			type_tv.setText("类型：活性炭GAC");
			time_tv.setText("时间："+(FilterLifeList[1])+"小时");
			break;
		case 3:
			type_tv.setText("类型：活性炭CTO");
			time_tv.setText("时间："+(FilterLifeList[2])+"小时");
			break;
		case 4:
			type_tv.setText("类型：RO膜");
			time_tv.setText("时间："+(FilterLifeList[3])+"小时");
			break;
		case 5:
			type_tv.setText("类型：活性炭T33");
			time_tv.setText("时间："+(FilterLifeList[4])+"小时");
			break;
		}
	}
	
	private void dismissFilterMsg(){
		llFilterMsg.setVisibility(View.GONE);
	}
	
	private void setFilterClean(){
		purifier_btn.setBackgroundResource(R.drawable.button_disable);
		clean_btn.setBackgroundResource(R.drawable.button_filter_clean);
		device_tv.setText("正在净水中");
	}
	
	private void setClean(){
		clean_btn.setBackgroundResource(R.drawable.button_disable);
		purifier_btn.setBackgroundResource(R.drawable.button_clean);
		device_tv.setText("正在冲洗中");
	}
	
	private void setNormal(){
		purifier_btn.setBackgroundResource(R.drawable.button_clean);
		clean_btn.setBackgroundResource(R.drawable.button_filter_clean);
		device_tv.setText("运行良好");
	}
	// ==================================================================================

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#didReceiveData(com.xtremeprog
	 * .xpgconnect.XPGWifiDevice, java.util.concurrent.ConcurrentHashMap, int)
	 */
	@Override
	protected void didReceiveData(XPGWifiDevice device,
			ConcurrentHashMap<String, Object> dataMap, int result) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;

		this.deviceDataMap = dataMap;
		handler.sendEmptyMessage(handler_key.RECEIVED.ordinal());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onBackPressed()
	 */
	@Override
	public void onBackPressed() {
		if (mView.isOpen()) {
			mView.toggle();
		} else {
			if (mXpgWifiDevice != null && mXpgWifiDevice.isConnected()) {
				mCenter.cDisconnect(mXpgWifiDevice);
				DisconnectOtherDevice();
			}
			finish();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.gizwits.aircondition.activity.BaseActivity#didDisconnected(com.xtremeprog
	 * .xpgconnect.XPGWifiDevice)
	 */
	@Override
	protected void didDisconnected(XPGWifiDevice device) {
		if (!device.getDid().equalsIgnoreCase(mXpgWifiDevice.getDid()))
			return;

		handler.sendEmptyMessage(handler_key.DISCONNECTED.ordinal());
	}

	/**
	 * 把状态信息存入表
	 * 
	 * @param map
	 *            the map
	 * @param json
	 *            the json
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void inputDataToMaps(ConcurrentHashMap<String, Object> map,
			String json) throws JSONException {
		Log.i("revjson", json);
		JSONObject receive = new JSONObject(json);
		Iterator actions = receive.keys();
		while (actions.hasNext()) {

			String action = actions.next().toString();
			Log.i("revjson", "action=" + action);
			// 忽略特殊部分
			if (action.equals("cmd") || action.equals("qos")
					|| action.equals("seq") || action.equals("version")) {
				continue;
			}
			JSONObject params = receive.getJSONObject(action);
			Log.i("revjson", "params=" + params);
			Iterator it_params = params.keys();
			while (it_params.hasNext()) {
				String param = it_params.next().toString();
				Object value = params.get(param);
				map.put(param, value);
				Log.i(TAG, "Key:" + param + ";value" + value);
			}
		}
		handler.sendEmptyMessage(handler_key.UPDATE_UI.ordinal());
	}
	
	/**
	 * Input alarm to list.(Show number of Alarm)
	 * 
	 * @param json
	 *            the json
	 * @throws JSONException
	 *             the JSON exception
	 */
	private void inputAlarmToList(String json){
		Log.i("revjson", json);
		JSONObject receive;
		try {
			receive = new JSONObject(json);
			Iterator actions = receive.keys();
			while (actions.hasNext()) {
				Log.i("revjson", "action");
				String action = actions.next().toString();
				DeviceAlarm alarm = new DeviceAlarm(DateUtil.getDateCN(new Date()),
						action);
				alarmList.add(alarm);
			}
			handler.sendEmptyMessage(handler_key.ALARM.ordinal());
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void OpenFinish() {
	}

	@Override
	public void CloseFinish() {
		backToMain();
	}

}
