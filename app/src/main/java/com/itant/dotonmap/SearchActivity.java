package com.itant.dotonmap;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;
import com.itant.dotonmap.bean.LocationBean;
import com.itant.library.recyclerview.CommonAdapter;
import com.itant.library.recyclerview.base.ViewHolder;
import com.umeng.analytics.MobclickAgent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jason on 2018/11/18.
 */

public class SearchActivity extends AppCompatActivity implements TextWatcher {
    private EditText et_search;
    private SuggestionSearch mSuggestionSearch;
    private String mCity;

    private List<LocationBean> mLocationList;
    private CommonAdapter<LocationBean> mAdapter;
    private RecyclerView rv_search;

    private OnGetSuggestionResultListener listener = new OnGetSuggestionResultListener() {
        public void onGetSuggestionResult(SuggestionResult res) {

            if (res == null || res.getAllSuggestions() == null) {
                return;
            }

            List<SuggestionResult.SuggestionInfo> infos = res.getAllSuggestions();
            mLocationList.clear();
            for (SuggestionResult.SuggestionInfo info : infos) {
                if (info.getPt() == null) {
                    continue;
                }
                LocationBean locationBean = new LocationBean();
                locationBean.setCity(info.getCity());
                locationBean.setLat(info.getPt().latitude);
                locationBean.setLate6(info.getPt().latitudeE6);
                locationBean.setLng(info.getPt().longitude);
                locationBean.setLnge6(info.getPt().longitudeE6);
                locationBean.setName(info.getKey());
                if (!TextUtils.isEmpty(info.getDistrict())) {
                    locationBean.setAddress(info.getCity() + "-" + info.getDistrict());
                } else {
                    locationBean.setAddress(info.getCity());
                }
                mLocationList.add(locationBean);
            }

            mAdapter.notifyDataSetChanged();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        mSuggestionSearch = SuggestionSearch.newInstance();
        mSuggestionSearch.setOnGetSuggestionResultListener(listener);

        et_search = findViewById(R.id.et_search);
        int code = getIntent().getIntExtra(MainActivity.KEY_REQUEST_CODE, 0);
        mCity = getIntent().getStringExtra(MainActivity.KEY_REQUEST_CITY);
        switch (code) {
            case MainActivity.REQUEST_CODE_1:
                et_search.setHint("请输入位置1");
                break;
            case MainActivity.REQUEST_CODE_2:
                et_search.setHint("请输入位置2");
                break;
            case MainActivity.REQUEST_CODE_3:
                et_search.setHint("请输入位置3");
                break;
        }

        et_search.addTextChangedListener(this);

        rv_search = findViewById(R.id.rv_search);
        final LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rv_search.setLayoutManager(layoutManager);
        mLocationList = new ArrayList<>();
        mAdapter = new CommonAdapter<LocationBean>(this, R.layout.item_search_result, mLocationList) {
            @Override
            protected void convert(ViewHolder holder, final LocationBean locationBean, int position) {
                holder.setText(R.id.tv_name, locationBean.getName());
                holder.setText(R.id.tv_address, locationBean.getAddress());
                holder.setOnClickListener(R.id.ll_search_result, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        onResultClick(locationBean);
                    }
                });
            }
        };
        rv_search.setAdapter(mAdapter);
    }

    private void onResultClick(LocationBean locationBean) {
        Intent intent = getIntent();
        intent.putExtra(MainActivity.KEY_SELECTED_LOCATION, locationBean);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        String keyword = s.toString();
        if (TextUtils.isEmpty(keyword)) {
            return;
        }

        String validKeyword = keyword.replaceAll(" ", "");
        if (TextUtils.isEmpty(validKeyword)) {
            return;
        }

        mSuggestionSearch.requestSuggestion((new SuggestionSearchOption())
                .keyword(validKeyword)
                .city(mCity));
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }
    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mSuggestionSearch != null) {
            mSuggestionSearch.destroy();
        }
    }
}
