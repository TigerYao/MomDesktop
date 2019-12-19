package com.huatu.tiger.theagedlauncher.page;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RadioButton;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.huatu.tiger.theagedlauncher.R;
import com.huatu.tiger.theagedlauncher.adapter.SimpleCommonRVAdapter;
import com.huatu.tiger.theagedlauncher.base.BaseFragment;
import com.huatu.tiger.theagedlauncher.bean.AppInfo;
import com.huatu.tiger.theagedlauncher.utils.DisplayUtil;
import com.huatu.tiger.theagedlauncher.utils.LoadAppUtils;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class AllAppFragment extends BaseFragment {
    private RecyclerView mRecyclerView;
    private ImageView mImgView;
    private RecyclerView.LayoutManager layoutManager;
    private SimpleCommonRVAdapter mAdapter;
    private int type;
    private AppFragmentListenner mAppFragmentListenner;

    public static AllAppFragment getFragment(Bundle args) {
        AllAppFragment fragment = new AllAppFragment();
        fragment.setArguments(args);
        return fragment;
    }

    public static AllAppFragment getFragment(int type) {
        Bundle args = new Bundle();
        args.putInt("type", type);
        return getFragment(args);
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
        layoutManager = new GridLayoutManager(getContext(), 2);
        mRecyclerView.setLayoutManager(layoutManager);
        LinearSnapHelper linearSnapHelper = new LinearSnapHelper();
        linearSnapHelper.attachToRecyclerView(mRecyclerView);
        mImgView = view.findViewById(R.id.img);
        mImgView.setOnClickListener((View v) -> {
            if (mAdapter.getSelected() != null && mAppFragmentListenner != null) {
                if (type == LoadAppUtils.ADD_CONTACT_TYPE)
                    mAppFragmentListenner.onContactsAdded(mAdapter.getSelected());
                else
                    mAppFragmentListenner.onAppAdded(mAdapter.getSelected());
            }
        });
        mRecyclerView.setBackgroundResource(R.mipmap.green);
        refershData(false);
    }

    public void setAppFragmentListenner(AppFragmentListenner appFragmentListenner) {
        this.mAppFragmentListenner = appFragmentListenner;
    }

    public void refershData(boolean refresh) {
        Observable.just(1)
                .map(new Function<Integer, List<AppInfo>>() {
                    @Override
                    public List<AppInfo> apply(Integer integer) throws Exception {
                        return type == LoadAppUtils.ADD_CONTACT_TYPE ? LoadAppUtils.getInstance(getActivity()).getContactList(refresh) : LoadAppUtils.getInstance(getActivity()).getAllApps(refresh);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<List<AppInfo>>() {
                    @Override
                    public void accept(List<AppInfo> appInfos) throws Exception {
                        updateInfos(appInfos);
                    }
                });
    }

    public void updateInfos(List<AppInfo> appInfos) {
        if (mAdapter == null) {
            mAdapter = new SimpleCommonRVAdapter<AppInfo>(appInfos, R.layout.cell_item, getContext()) {
                List<AppInfo> mSelectedInfos = type == LoadAppUtils.ADD_APP_TYPE ? LoadAppUtils.getInstance(getActivity()).mDefaltInfos : LoadAppUtils.getInstance(getActivity()).mDefaultContacts;

                public List<AppInfo> getSelected() {
                    return mSelectedInfos;
                }

                @Override
                public void convert(SimpleViewHolder holder, AppInfo item, int position) {
                    holder.itemView.setLayoutParams(new ViewGroup.LayoutParams(DisplayUtil.realWidh, DisplayUtil.realHeight));
                    holder.setText(R.id.name, item.label);
                    holder.setImageResource(R.id.icon, item.icon);
                    RadioButton radioButton = holder.getView(R.id.select_options);
                    if (type == LoadAppUtils.ADD_APP_TYPE || type == LoadAppUtils.ADD_CONTACT_TYPE) {
                        if (mSelectedInfos == null)
                            mSelectedInfos = new ArrayList<>();
                        else
                            radioButton.setChecked(mSelectedInfos.contains(item));
                        radioButton.setVisibility(View.VISIBLE);

                    }
                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (type == LoadAppUtils.ALL_APP_TYPE) {
                                Intent intent = new Intent();
                                intent.setClassName(item.packageName, item.appName);
                                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            } else if (type == LoadAppUtils.ADD_APP_TYPE && mAppFragmentListenner != null) {
                                boolean isContainer = mSelectedInfos.contains(item);
                                if (isContainer) {
                                    mSelectedInfos.remove(item);
                                    item.type = LoadAppUtils.APP_TYPE;
                                } else {
                                    item.type = LoadAppUtils.DEFAULT_APP_TYPE;
                                    mSelectedInfos.add(mSelectedInfos.size() - 1, item);
                                }
                                radioButton.setChecked(!isContainer);
                            } else if (type == LoadAppUtils.ADD_CONTACT_TYPE) {
                                boolean isContainer = mSelectedInfos.contains(item);
                                if (isContainer) {
                                    mSelectedInfos.remove(item);
                                    item.type = LoadAppUtils.ALL_CONTACT_TYPE;
                                } else {
                                    item.type = LoadAppUtils.DEFAULT_CONTACT_TYPE;
                                    mSelectedInfos.add(mSelectedInfos.size() - 1, item);
                                }
                                radioButton.setChecked(!isContainer);
                            }
                        }
                    });
                }
            };
            mRecyclerView.setAdapter(mAdapter);
        } else
            mAdapter.setData(appInfos);
    }

    public interface AppFragmentListenner {
        void onAppAdded(List<AppInfo> info);

        void onContactsAdded(List<AppInfo> info);
    }
}
