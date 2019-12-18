package com.huatu.tiger.theagedlauncher.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.huatu.tiger.theagedlauncher.R;
import com.huatu.tiger.theagedlauncher.adapter.SimpleCommonRVAdapter;
import com.huatu.tiger.theagedlauncher.base.BaseFragment;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;
import com.huatu.tiger.theagedlauncher.utils.LoadAppUtils;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class AllAppFragment extends BaseFragment {
    RecyclerView mRecyclerView;
    ImageView mImgView;
    RecyclerView.LayoutManager layoutManager;
    SimpleCommonRVAdapter mAdapter;
    int type;
    AppFragmentListenner mAppFragmentListenner;
    public static AllAppFragment getFragment(Bundle args) {
        AllAppFragment fragment = new AllAppFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public static AllAppFragment getFragment(int type) {
        AllAppFragment fragment = new AllAppFragment();
        Bundle args = new Bundle();
        args.putInt("type", type);
        fragment.setArguments(args);
        return fragment;
    }

    public void setAppFragmentListenner(AppFragmentListenner appFragmentListenner) {
        this.mAppFragmentListenner = appFragmentListenner;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        type = getArguments().getInt("type");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.cell_list);
        layoutManager =new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(mRecyclerView);
        mImgView = view.findViewById(R.id.img);
        mImgView.setOnClickListener((View v) -> {
            if (layoutManager instanceof LinearLayoutManager)
                layoutManager = new GridLayoutManager(getContext(), 2);
            else
                layoutManager = new LinearLayoutManager(getContext());
            mRecyclerView.setLayoutManager(layoutManager);
            mAdapter.notifyDataSetChanged();
        });
        mRecyclerView.setBackgroundResource(R.mipmap.green);
        LoadAppUtils.getInstance(getActivity()).getAllApps().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<AppInfo>>() {
            @Override
            public void accept(List<AppInfo> appInfos) throws Exception {
               updateInfos(appInfos);
            }
        });
    }

    public void updateInfos(List<AppInfo> appInfos){
        if (mAdapter == null) {
            mAdapter = new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, getContext()) {

                @Override
                public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                    holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.realWidh, DisplayUtil.realHeight));
                    holder.setText(R.id.name, item.label);
                    holder.setImageResource(R.id.icon, item.icon);
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(type == LoadAppUtils.ALL_APP_TYPE) {
                                Intent intent = new Intent();
                                intent.setClassName(item.packageName, item.appName);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }else if(type == LoadAppUtils.ADD_APP_TYPE && mAppFragmentListenner != null){
                                item.type = LoadAppUtils.DEFAULT_APP_TYPE;
                                LoadAppUtils.getInstance(getContext()).getAppDao().insertApps(item);
                                mAppFragmentListenner.onAppAdded(item);
                            }
                        }
                    });
                }
            };
            mRecyclerView.setAdapter(mAdapter);
        } else
            mAdapter.setData(appInfos);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,  String[] permissions,  int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onBackPress() {

    }

    public interface AppFragmentListenner{
        void onAppAdded(AppInfo info);
    }
}
