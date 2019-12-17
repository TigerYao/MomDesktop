package com.huatu.tiger.theagedlauncher.page;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.huatu.tiger.theagedlauncher.R;
import com.huatu.tiger.theagedlauncher.adapter.SimpleCommonRVAdapter;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.utils.LoadAppUtils;
import com.huatu.tiger.theagedlauncher.view.PageIndicator;

import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;

public class AllAppFragment extends Fragment {
    RecyclerView mRecyclerView;

    public static AllAppFragment getFragment(Bundle args){
        AllAppFragment fragment = new AllAppFragment();
        fragment.setArguments(args);
        return fragment;
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.contact_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mRecyclerView = view.findViewById(R.id.cell_list);
//        PagerSnapHelper pagerSnapHelper = new PagerSnapHelper();
//        pagerSnapHelper.attachToRecyclerView(mRecyclerView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(linearLayoutManager);
        LoadAppUtils.getInstance(getActivity()).getAllApps().observeOn(AndroidSchedulers.mainThread()).subscribe(new Consumer<List<AppInfo>>() {
            @Override
            public void accept(List<AppInfo> appInfos) throws Exception {
                mRecyclerView.setAdapter(new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_grid_item, getContext()) {
                    @Override
                    public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                        holder.setText(R.id.name, item.label);
                        holder.setImageResource(R.id.icon, item.icon);
                    }
                });
            }
        });
    }
}
