package poi.map.com.mappoi;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amap.api.services.core.PoiItem;
import com.amap.api.services.poisearch.PoiResult;
import com.amap.api.services.poisearch.PoiSearch;

import java.util.ArrayList;
import java.util.List;

import poi.map.com.mappoi.Model.PoiModel;

public class SearchAddressActivity extends AppCompatActivity implements View.OnClickListener, PoiSearch.OnPoiSearchListener {

    private EditText edAdress;
    private ListView lv;
    private List<PoiModel> data = new ArrayList<>();
    private LvAdapter lvAdapter;
    private PoiSearch.Query query;
    private PoiSearch poiSearch;
    private String address = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_address);
        initData();
    }

    private void initData() {
        edAdress = (EditText) findViewById(R.id.ed_address);
        lv = (ListView) findViewById(R.id.lv);

        edAdress.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!TextUtils.isEmpty(s.toString())) {
                    query = new PoiSearch.Query(s.toString(), "", "");
                    query.setPageSize(10);
                    query.setPageNum(0);
                    poiSearch = new PoiSearch(SearchAddressActivity.this, query);
                    poiSearch.setOnPoiSearchListener(SearchAddressActivity.this);
                    poiSearch.searchPOIAsyn();
                }
            }
        });
        lvAdapter = new LvAdapter();
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                for (int i = 0; i < data.size(); i++) {
                    data.get(i).setChecked(false);
                }
                data.get(position).setChecked(true);
                lvAdapter.notifyDataSetChanged();
                Intent intent = new Intent();
                intent.putExtra("LocationX", data.get(position).getLocationX());
                intent.putExtra("LocationY", data.get(position).getLocationY());
                intent.putExtra("cityCode", data.get(position).getCityCode());
                intent.putExtra("addressName", address);
                setResult(RESULT_OK, intent);
                SearchAddressActivity.this.finish();
            }
        });
        lv.setAdapter(lvAdapter);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_N:
                finish();
                break;
        }
    }

    @Override
    public void onPoiSearched(PoiResult result, int resultCode) {

        if (resultCode == 1000) {
            data.removeAll(data);
            address = result.getQuery().getQueryString();
            for (int i = 0; i < result.getPois().size(); i++) {
                if (i == 0) {
                    data.add(new PoiModel(result.getPois().get(i).toString(), result.getPois().get(i).getCityName(),
                            result.getPois().get(i).getProvinceName(),
                            result.getPois().get(i).getCityCode(),
                            result.getPois().get(i).getProvinceCode(),
                            result.getPois().get(i).getSnippet(),
                            result.getPois().get(i).getLatLonPoint().getLongitude(),
                            result.getPois().get(i).getLatLonPoint().getLatitude(),
                            true));
                } else {
                    data.add(new PoiModel(result.getPois().get(i).toString(), result.getPois().get(i).getCityName(),
                            result.getPois().get(i).getProvinceName(),
                            result.getPois().get(i).getCityCode(),
                            result.getPois().get(i).getProvinceCode(),
                            result.getPois().get(i).getSnippet(),
                            result.getPois().get(i).getLatLonPoint().getLongitude(),
                            result.getPois().get(i).getLatLonPoint().getLatitude(),
                            false));
                }
                lvAdapter.notifyDataSetChanged();
            }
        }
    }

    @Override
    public void onPoiItemSearched(PoiItem poiItem, int i) {

    }

    class LvAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            SearchAddressActivity.viewHolder vh = null;
            if (convertView == null) {
                vh = new SearchAddressActivity.viewHolder();
                convertView = View.inflate(SearchAddressActivity.this, R.layout.item_map, null);
                vh.iv_img = (ImageView) convertView.findViewById(R.id.iv_img);
                vh.tv_name = (TextView) convertView.findViewById(R.id.tv_name);
                vh.tv_address_name = (TextView) convertView.findViewById(R.id.tv_address_name);
                convertView.setTag(vh);
            } else {
                vh = (SearchAddressActivity.viewHolder) convertView.getTag();
            }
            if (data.get(position).isChecked()) {
                vh.iv_img.setBackgroundResource(R.drawable.icon_choice_bjg_h);
            } else {
                vh.iv_img.setBackgroundResource(R.drawable.icon_choice_bjg_d);
            }
            vh.tv_name.setText(data.get(position).getProvince() + data.get(position).getCity() + data.get(position).getSnippet());
            vh.tv_address_name.setText(data.get(position).getAddressName());

            return convertView;
        }
    }

    class viewHolder {
        ImageView iv_img;
        TextView tv_name;
        TextView tv_address_name;
    }
}
