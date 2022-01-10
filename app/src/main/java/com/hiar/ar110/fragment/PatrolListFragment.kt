@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.hiar.ar110.fragment

import android.app.AlertDialog
import android.view.View
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.adapter.PatrolListAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.extension.setOnThrottledClickListener
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.service.AR110BaseService
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.PatrolListViewModel
import com.hiar.ar110.widget.SpaceDecoration
import com.hiar.mybaselib.utils.AR110Log

/**
 * Author:wilson.chen
 * date：5/25/21
 * desc：
 */
class PatrolListFragment : BaseFragment() {
    private lateinit var mLayoutBack: View
    private lateinit var mRecyclerView: RecyclerView
    private lateinit var contentView: View
    private var isRefreshData = false
    private var hasLoadMore = true
    private var mIsLoading = false
    private val mDatas by lazy {
        mutableListOf<PatrolRecord>()
    }
    private val mAdapter by lazy {
        PatrolListAdapter(mDatas) { position ->
            if (!AR110BaseService.isInitialized) {
                return@PatrolListAdapter
            }
            if (mIsLoading) {
                Util.showMessage("正在刷新警单，请稍后...")
                return@PatrolListAdapter
            }
            NavigationHelper.instance.beginHandleJD(null, mDatas[position])
        }
    }
    private val mViewModel by lazy {
        getViewModel(PatrolListViewModel::class.java)
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_patrol
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
        contentView.findViewById<View>(R.id.text_create_task)
                .setOnThrottledClickListener(1000 * 2) {
                    mViewModel.createPopulationTask()
                }
    }

    override fun initObserver() {
        mViewModel.creatTaskResult.observe(this, Observer { result ->
            result ?: return@Observer
            if (result.retCode == 12) {
                showConfirmDlg()
                return@Observer
            }
            if (result.isSuccess) {
                NavigationHelper.instance.beginHandleJD(null, result.data)
            }
        })

        mViewModel.modifyPatrolState.observe(this, Observer { result ->
            result ?: return@Observer
            AR110Log.i(TAG, "finish report cjzt:$result")
            if (!AR110MainActivity.isPushing) {
                AR110BaseService.instance!!.sendPatrolVideouploadTask(mDatas[0])
            }
            refreshPatrolTask(true)
        })

        mViewModel.getPatrolTaskRecordState.observe(this, Observer { result ->
            if (result.isSuccess) {
                if (isRefreshData) {
                    mDatas.clear()
                    isRefreshData = false
                }
                result.data?.patroList?.let {
                    hasLoadMore = it.size >= ConstantApp.MAX_FETCH_NUM
                    mDatas.addAll(it)
                }
                mAdapter.notifyDataSetChanged()
            }
            mIsLoading = false
        })
        mViewModel.httpErrorMessage.observe(this, Observer {
            Util.showMessage(it.message)
        })
    }

    override fun onResume() {
        super.onResume()
        refreshPatrolTask(true)
    }

    override fun onBackPressed() {
//        return false
        NavigationHelper.Companion.instance.backToHomePage(this)
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
                        refreshPatrolTask(false)
                    }
                }
            })
        }
    }

    private fun refreshPatrolTask(isRefresh: Boolean = false) {
        if (mIsLoading) {
            return
        }
        this.isRefreshData = isRefresh
        mIsLoading = true
        if (isRefresh) {
            mViewModel.getPatrolTaskRecord(0)
        } else {
            mViewModel.getPatrolTaskRecord(mDatas.size)
        }
    }

    private fun showConfirmDlg() {
        val alertdialogbuilder = AlertDialog.Builder(context)
        val view = View.inflate(activity, R.layout.layout_mydialog, null)
        alertdialogbuilder.setView(view)
        val mDialog = alertdialogbuilder.create()
        mDialog.show()
        mDialog.setCancelable(false)
        val width = resources.getDimension(R.dimen.dp320).toInt()
        val height = resources.getDimension(R.dimen.dp166).toInt()
        mDialog.getWindow()?.setLayout(width, height)
        val mTextNo = view.findViewById<TextView>(R.id.text_cancel)
        mTextNo.setOnThrottledClickListener { v: View? -> mDialog.dismiss() }
        val mTextYes = view.findViewById<TextView>(R.id.text_ok)
        mTextYes.setOnThrottledClickListener {
            mDialog.dismiss()
            val pRecord = mDatas[0]
            mViewModel.modifyPatrolState("${pRecord.id}")
            pRecord.status = 1
        }
    }
}