@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.hiar.ar110.fragment

import android.view.View
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.R
import com.hiar.ar110.adapter.PopulationListAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.extension.setOnThrottledClickListener
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.helper.NavigationHelper.Companion.instance
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.util.showConfirmDialog
import com.hiar.ar110.viewmodel.PopulationViewModel
import com.hiar.ar110.widget.SpaceDecoration
import com.hiar.mybaselib.utils.AR110Log

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */
class PopulationFragment : BaseFragment() {
    private lateinit var mLayoutBack: View
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var contentView: View
    private lateinit var mCreateTaskView: View
    private var isRefreshData = false
    private var hasLoadMore = true
    private var mIsLoading = false
    private val mDatas by lazy {
        mutableListOf<PopulationRecord>()
    }
    private val mAdapter by lazy {
        PopulationListAdapter(mDatas) { position ->
            onItemClick(position)
        }
    }

    private val mViewModel by lazy {
        getViewModel(PopulationViewModel::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.activity_h_c_list
    }

    override fun initData() {}

    override fun initView(contentView: View) {
        this.contentView = contentView
        mLayoutBack = contentView.findViewById<View>(R.id.layout_back)
        mRecyclerView = contentView.findViewById(R.id.recycler_view)
        initRecycle()
    }

    override fun initListener() {
        mLayoutBack.setOnThrottledClickListener { v: View? -> NavigationHelper.instance.beginHomePage() }
        mCreateTaskView = contentView.findViewById<View>(R.id.text_create_task).apply {
            setOnThrottledClickListener(1000 * 1) {
                mCreateTaskView.isEnabled = false
                mViewModel.createTask()
            }
        }
    }

    override fun initObserver() {
        mViewModel.creatTaskResult.observe(this, Observer { result ->
            mCreateTaskView.isEnabled = true
            result ?: return@Observer
            if (result.retCode == 12) {
                showConfirmDlg()
                return@Observer
            }
            if (result.isSuccess) {
                instance.beginHandlePopulation(result.data)
            }
        })

        mViewModel.modifyState.observe(this, Observer { result ->
            mCreateTaskView.isEnabled = true
            result ?: return@Observer
            if (result.isSuccess) {
                AR110Log.i(TAG, "finish report cjzt:$result")
                refreshPopulationTask(true)
            } else {
                Util.showMessage(getString(R.string.unknown_error))
            }
        })

        mViewModel.getTaskRecordState.observe(this, Observer { result ->
            if (result.isSuccess) {
                if (isRefreshData) {
                    mDatas.clear()
                    isRefreshData = false
                }
                result.data?.verificationList?.let {
                    hasLoadMore = it.size >= ConstantApp.MAX_FETCH_NUM
                    mDatas.addAll(it)
                }
                mAdapter.notifyDataSetChanged()
            }
            mIsLoading = false
        })

        mViewModel.httpErrorMessage.observe(this, Observer {
            Util.showMessage(it.message)
            mIsLoading = false
            mCreateTaskView.isEnabled = true
        })
    }

    override fun onResume() {
        super.onResume()
        refreshPopulationTask(true)
    }

    override fun onBackPressed() {
        instance.beginHomePage()
    }

    private fun initRecycle() {
        mRecyclerView.apply {
            val llm = LinearLayoutManager(activity)
            layoutManager = llm
            addItemDecoration(SpaceDecoration(0,
                    resources?.getDimension(R.dimen.dp12)?.toInt() ?: 0))
            adapter = mAdapter
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    val lastVisibleItem = llm.findLastVisibleItemPosition()
                    val totalItemCount = llm.itemCount
                    if (lastVisibleItem >= totalItemCount - 2 && dy > 0 && hasLoadMore) {
                        refreshPopulationTask(false)
                    }
                }
            })
        }
    }

    private fun onItemClick(position: Int) {
        if (!AR110BaseService.isInitialized) {
            return
        }
        if (mIsLoading) {
            Util.showMessage("正在刷新列表，请稍后...")
            return
        }
        val temp = mDatas[position];
        instance.beginHandlePopulation(temp)
    }

    private fun refreshPopulationTask(isRefresh: Boolean = false) {
        if (mIsLoading) {
            return
        }
        this.isRefreshData = isRefresh
        mIsLoading = true
        if (isRefresh) {
            mViewModel.getTaskRecord(0)
        } else {
            mViewModel.getTaskRecord(mDatas.size)
        }
    }

    private fun showConfirmDlg() {
        context?.let {
            showConfirmDialog(it, "有处理中的核查任务，请先结束") {
                val pRecord = mDatas[0]
                mViewModel.modifyState("${pRecord.id}")
                pRecord.status = 1
                mCreateTaskView.isEnabled = false
            }
        }
    }
}