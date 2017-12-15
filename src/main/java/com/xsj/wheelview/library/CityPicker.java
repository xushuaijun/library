package com.xsj.wheelview.library;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.handmark.pulltorefresh.library.R;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * 城市Picker
 * 
 * @author zihao
 * 
 */
public class CityPicker extends LinearLayout {
	/** 滑动控件 */
	private ScrollerNumberPicker provincePicker;
	private ScrollerNumberPicker cityPicker;
	/** 选择监听 */
	private OnSelectingListener onSelectingListener;
	/** 刷新界面 */
	private static final int REFRESH_VIEW = 0x001;
	/** 临时日期 */
	private int tempProvinceIndex = -1;
	private int temCityIndex = -1;
	private Context context;
	private List<CityData> province_list = new ArrayList<CityData>();
	private HashMap<String, List<CityData>> city_map = new HashMap<String, List<CityData>>();

	private CitycodeUtil citycodeUtil;
	private String city_code_string;
	private String city_string;
	public testCity city;
	private int provice_code;
	private int city_code;

	public CityPicker(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		getaddressinfo();
		// TODO Auto-generated constructor stub
	}

	public CityPicker(Context context) {
		super(context);
		this.context = context;
		getaddressinfo();
		// TODO Auto-generated constructor stub
	}
	
	public void setCity(testCity testCityWbb){
		this.city=testCityWbb;
	}

	// 获取城市信息
	private void getaddressinfo() {
		// TODO Auto-generated method stub
		// 读取城市信息string
		JSONParser parser = new JSONParser();
		String cityjson = AppJsonFileReader.getJson(getContext(), "city.json");
		// 形成数据
		parser.getJSONParserResult(cityjson);

		/*Executors.newSingleThreadExecutor().execute(new Runnable() {
			@Override
			public void run() {

			}
		});*/

	}

	public class JSONParser {

		public void  getJSONParserResult(String JSONString) {
			// 获取用户类型
			Type type= new TypeToken<List<CityData>>(){}.getType();
			// 根据类型返回实体类

			Gson gson = new Gson();
			List<CityData>  datamessage = gson.fromJson(JSONString, type);
			if(datamessage!=null && datamessage.size()>0){
				int state_code=-1;
				for(int i=0;i<datamessage.size();i++){
					CityData cityData=new CityData();
					// 身份id,身份名称
					int tempcode=datamessage.get(i).getState_code();
					String tempname=datamessage.get(i).getState_name();
                    // 城市id,城市名称
					int id=datamessage.get(i).getId();
					String cityname=datamessage.get(i).getName();

					// 形成省集合，市map
					if(state_code!=tempcode){
						cityData.setState_code(tempcode);
						cityData.setState_name(tempname);
						cityData.setId(id);
						cityData.setName(cityname);
						state_code=tempcode;
						province_list.add(cityData);
						List<CityData> citylist=new ArrayList<CityData>();
						citylist.add(cityData);
						city_map.put(String.valueOf(tempcode),citylist);
					}else{
						cityData.setState_code(tempcode);
						cityData.setState_name(tempname);
						cityData.setId(id);
						cityData.setName(cityname);
                        city_map.get(String.valueOf(tempcode)).add(cityData);

					}


				}


			}

		}

	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		LayoutInflater.from(getContext()).inflate(R.layout.city_picker, this);
		citycodeUtil = CitycodeUtil.getSingleton();
		// 获取控件引用
		provincePicker = (ScrollerNumberPicker) findViewById(R.id.province);

		cityPicker = (ScrollerNumberPicker) findViewById(R.id.city);
//		counyPicker = (ScrollerNumberPicker) findViewById(R.id.couny);
		provincePicker.setData(citycodeUtil.getProvince(province_list));
		provincePicker.setDefault(1);
		provice_code=Integer.valueOf(citycodeUtil.getProvince_list_code().get(1));
		cityPicker.setData(citycodeUtil.getCity(city_map, citycodeUtil
				.getProvince_list_code().get(1)));
		cityPicker.setDefault(1);
		city_code=city_map.get(String.valueOf(provice_code)).get(1).getId();
		provincePicker.setOnSelectListener(new ScrollerNumberPicker.OnSelectListener() {

			@Override
			public void endSelect(int id, String text) {
				// TODO Auto-generated method stub
				// 省份编码
				provice_code=Integer.valueOf(citycodeUtil.getProvince_list_code().get(id));
				city_code=city_map.get(String.valueOf(provice_code)).get(1).getId();

				if (text.equals("") || text == null)
					return;
				if (tempProvinceIndex != id) {
					String selectDay = cityPicker.getSelectedText();
					if (selectDay == null || selectDay.equals(""))
						return;
					// 城市数组
					cityPicker.setData(citycodeUtil.getCity(city_map,
							citycodeUtil.getProvince_list_code().get(id)));
					cityPicker.setDefault(1);
					//得到市级
					String shi=cityPicker.getSelectedText();
					//设置 值
					city.cityAll(text,shi,provice_code,city_code);

					int lastDay = Integer.valueOf(provincePicker.getListSize());
					if (id > lastDay) {
						provincePicker.setDefault(lastDay - 1);
					}
				}
				tempProvinceIndex = id;
				Message message = new Message();
				message.what = REFRESH_VIEW;
				handler.sendMessage(message);
			}

			@Override
			public void selecting(int id, String text) {
				// TODO Auto-generated method stub
			}
		});
		cityPicker.setOnSelectListener(new ScrollerNumberPicker.OnSelectListener() {

			@Override
			public void endSelect(int id, String text) {
				// TODO Auto-generated method stub

				// 城市编码

				city_code=city_map.get(String.valueOf(provice_code)).get(id).getId();


				if (text.equals("") || text == null)
					return;
				if (temCityIndex != id) {

					String selectDay = provincePicker.getSelectedText();
					if (selectDay == null || selectDay.equals(""))
						return;
					//设置 值
					city.cityAll(selectDay,text,provice_code,city_code);

					int lastDay = Integer.valueOf(cityPicker.getListSize());
					if (id > lastDay) {
						cityPicker.setDefault(lastDay - 1);
					}
				}
				temCityIndex = id;
				Message message = new Message();
				message.what = REFRESH_VIEW;
				handler.sendMessage(message);
			}

			@Override
			public void selecting(int id, String text) {
				// TODO Auto-generated method stub

			}
		});
	}


	Handler handler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			switch (msg.what) {
			case REFRESH_VIEW:
				if (onSelectingListener != null)
					onSelectingListener.selected(true);
				break;
			default:
				break;
			}
		}

	};

	public void setOnSelectingListener(OnSelectingListener onSelectingListener) {
		this.onSelectingListener = onSelectingListener;
	}

	public String getCity_code_string() {
		return city_code_string;
	}

	public String getCity_string() {
		city_string = provincePicker.getSelectedText()
				+" "+ cityPicker.getSelectedText();
		return city_string;
	}

	public interface OnSelectingListener {

		public void selected(boolean selected);
	}
	
	public interface testCity {
		
		public void cityAll(String sheng, String shi,int provice_code,int city_code);
		
	}
}
