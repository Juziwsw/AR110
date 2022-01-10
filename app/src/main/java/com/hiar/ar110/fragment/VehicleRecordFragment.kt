package com.hiar.ar110.fragment

import android.os.Bundle
import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.JsonSyntaxException
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.adapter.CarCropImgAdapter
import com.hiar.ar110.adapter.VehicleDateListAdapter
import com.hiar.ar110.adapter.VehicleListAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.vehicle.*
import com.hiar.ar110.helper.NavigationHelper
import com.hiar.ar110.listener.OnImageItemClickListener
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.VehicleRecordViewModel
import com.hiar.ar110.widget.RecyclerViewMultiClickListener
import com.hiar.ar110.widget.RecyclerViewMultiClickListener.SimpleOnItemClickListener
import com.hiar.ar110.widget.SpaceDecoration
import com.hiar.mybaselib.utils.AR110Log
import kotlinx.android.synthetic.main.fragment_vehicle_record.*
import kotlinx.coroutines.launch
import java.util.*

/**
 *
 * @author xuchengtang
 * @date 31/05/2021
 * Email: xucheng.tang@hiscene.com
 */

class VehicleRecordFragment : BaseFragment() {
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolTask: PatrolRecord? = null
    private var mVehicleListAdapter: VehicleListAdapter? = null
    private var mDateListAdapter: VehicleDateListAdapter? = null
    private var mCarCropAdapter: CarCropImgAdapter? = null
    private val mVehicleInfoList = ArrayList<VehicleRecogList>()
    private val mVehicleNotInLibList = ArrayList<RecoRecog>()
    private var mTotalItem = 0
    private var mNotInLibNum = 0
    private var mInLibNum = 0
    private var mCurrentDate: String? = null
    private val mIsBusy = false
    private val mDateList = ArrayList<String>()
    fun setNewDate(curdate: String) {
        recycler_view_datelist.visibility = View.GONE
        img_drop_icon.setImageResource(R.drawable.ic_icon_up)
        if (curdate != mCurrentDate) {
            text_date_sel.text = curdate
            mCurrentDate = curdate
            val dateArray = mCurrentDate!!.split("-").toTypedArray()
            val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
            text_date_sel.text = dateDisplay
            img_drop_icon.isClickable = true
            mCarCropAdapter!!.setAdapter(mVehicleNotInLibList, curdate)
        }
    }

    private fun parseDateInfo() {
        if (mVehicleNotInLibList.size == 0) {
            return
        }
        val len = mVehicleNotInLibList.size
        var lastDate: String? = null
        mDateList.clear()
        mVehicleNotInLibList.sortWith { o1, o2 ->
            val t1 = o1.gpsTime
            val t2 = o2.gpsTime
            if (t1.compareTo(t2) >= 0) {
                -1
            } else {
                0
            }
        }
        for (i in 0 until len) {
            if (null == lastDate) {
                lastDate = mVehicleNotInLibList[i].gpsTime.substring(0, 10)
                mDateList.add(lastDate)
            } else {
                val curDate = mVehicleNotInLibList[i].gpsTime.substring(0, 10)
                if (!lastDate.endsWith(curDate)) {
                    if (!mDateList.contains(curDate)) {
                        mDateList.add(curDate)
                        lastDate = curDate
                    }
                }
                mDateList.sortWith(Comparator { o1, o2 ->
                    if (o1.compareTo(o2) >= 0) {
                        -1
                    } else {
                        0
                    }
                })
            }
        }
    }

    override fun onHileiaComingCallCallback() {
        activity?.runOnUiThread { layout_commu_phone.visibility = View.VISIBLE }
    }

    override fun onHileiaHangupCallback() {
        activity?.runOnUiThread { layout_commu_phone.visibility = View.GONE }
    }

    override fun onStart() {
        super.onStart()
        fetchCarRecord()
        if (AR110MainActivity.isPushing) {
            layout_commu_phone.visibility = View.VISIBLE
        }
    }

    private fun fetchCarRecord() {
        with(mViewModel) {
            viewModelScope.launch {
                text_recog.isClickable = false
                text_unrecog.isClickable = false
                text_date_sel.isClickable = true
                fetchCarRecord(mCopTask,mPatrolTask)
            }
        }
    }
    private val mViewModel by lazy {
        getViewModel(VehicleRecordViewModel::class.java)
    }

    override fun initObserver() {
        mViewModel.vehicleRecordResult.observe(this, { result ->
            result?.let {
                if (result.isSuccess) {
                    AR110Log.d(TAG, "get vResult=${result.retCode} data=${result.data}")
                    try {
                        val vResult = result.data as VehicleRecord
                        AR110Log.d(TAG, "get vResult=$vResult")
                        if (vResult.carRecoList != null && vResult.carRecoList.isNotEmpty()) {
                            Util.mCarPhotoUrlHead = vResult.imageUrl
                            mVehicleInfoList.clear()
                            mVehicleNotInLibList.clear()
                            mCurrentDate = null
                            val len = vResult.carRecoList.size
                            mTotalItem = len
                            mNotInLibNum = 0
                            mInLibNum = 0
                            for (i in 0 until len) {
                                val item = vResult.carRecoList[i]
                                if (null != item) {
                                    if (item.recoStatus == Util.RECOG_STATUS_INLIB) {
                                        mVehicleInfoList.add(item)
                                    } else if (item.recoStatus == Util.RECOG_STATUS_NOTINLIB) {
                                        if (item.recoRecord != null && item.recoRecord.isNotEmpty()) {
                                            val itemNum = item.recoRecord.size
                                            mNotInLibNum++
                                            for (j in 0 until itemNum) {
                                                mVehicleNotInLibList.add(item.recoRecord[j])
                                            }
                                        }
                                    }
                                }
                            }
                            mInLibNum = mVehicleInfoList.size
                            if (mVehicleNotInLibList.size > 0) {
                                //mCurrentDate = mVehicleNotInLibList.get(0).gpsTime.substring(0,10);
                                parseDateInfo()
                                mCurrentDate = mVehicleNotInLibList[0].gpsTime.substring(0, 10)
                            }
                            val title = getString(R.string.vehicle_inspect_title)
                            val strInLib = getString(R.string.vehicle_has_recog) + " " + String.format(" (%d)", mInLibNum)
                            text_recog.text = strInLib
                            text_title.text = title
                            mVehicleListAdapter!!.setAdapter(mVehicleInfoList)
                            if (null != mCurrentDate) {
                                val dateArray = mCurrentDate!!.split("-").toTypedArray()
                                val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
                                text_date_sel.text = dateDisplay
                                img_drop_icon.isClickable = true
                                if (mNotInLibNum > 0) {
                                    img_drop_icon.visibility = View.VISIBLE
                                } else {
                                    img_drop_icon.visibility = View.INVISIBLE
                                }
                                img_drop_icon.setOnClickListener {
                                    if (recycler_view_datelist.visibility == View.VISIBLE) {
                                        recycler_view_datelist.visibility = View.INVISIBLE
                                        img_drop_icon.setImageResource(R.drawable.ic_icon_up)
                                    } else {
                                        mDateListAdapter!!.setAdapter(mDateList)
                                        recycler_view_datelist.visibility = View.VISIBLE
                                        img_drop_icon.setImageResource(R.drawable.ic_icon_down)
                                    }
                                }
                            } else {
                                img_drop_icon.visibility = View.INVISIBLE
                            }
                            text_recog.isClickable = true
                            text_unrecog.isClickable = true
                        } else {
                            mVehicleInfoList.clear()
                            mDateList.clear()
                            mVehicleNotInLibList.clear()

                            mVehicleListAdapter!!.resetAdapter()
                            mCarCropAdapter!!.resetAdapter()
                            mDateListAdapter!!.resetAdapter()
                        }
                    } catch (e: JsonSyntaxException) {
                    }
                    AR110Log.i(TAG, "observe res=$result")
                }
            }
        })
    }

    override fun onHiddenChanged(hidden: Boolean) {
        if (!hidden) {
            if (AR110MainActivity.isPushing) {
                layout_commu_phone.visibility = View.VISIBLE
            } else {
                layout_commu_phone.visibility = View.GONE
            }
            fetchCarRecord()
        }
    }

    private var firstVisibleItem = 0
    private val firstVisibleItemCrop = 0
    override fun getLayoutId(): Int {
        return R.layout.fragment_vehicle_record
    }

    override fun initData() {
        if (arguments != null) {
            mCopTask = arguments!!.getParcelable("key_cop_task")
            mPatrolTask = arguments!!.getParcelable("key_patrol_task")
        }
    }

    override fun initView(contentView: View) {
        img_drop_icon.visibility = View.INVISIBLE
        text_recog.isClickable = false
        text_unrecog.isClickable = false
        vehicle_list_notrecog.visibility = View.INVISIBLE
        var number: String? = ""
        if (null != mCopTask) {
            number = mCopTask!!.cjdbh2
        } else if (null != mPatrolTask) {
            number = mPatrolTask!!.number
        }
        mVehicleListAdapter = VehicleListAdapter(contentView.context.applicationContext, number!!, object : OnImageItemClickListener {
            override fun onItemClick(url: String) {
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        })
        val mLinearLayoutManager = LinearLayoutManager(activity)
        recycler_view_car.layoutManager = mLinearLayoutManager
        recycler_view_car.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp16).toInt()))
        recycler_view_car.adapter = mVehicleListAdapter
        mDateListAdapter = VehicleDateListAdapter(this)
        val mLinearLayoutManagerDate = LinearLayoutManager(activity)
        recycler_view_datelist.layoutManager = mLinearLayoutManagerDate
        recycler_view_datelist.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp1).toInt()))
        recycler_view_datelist.adapter = mDateListAdapter
        recycler_cropimg_view.addItemDecoration(SpaceDecoration(resources.getDimension(R.dimen.dp8).toInt(), resources.getDimension(R.dimen.dp4).toInt()))
        mCarCropAdapter = CarCropImgAdapter(activity!!.applicationContext)
        recycler_cropimg_view.adapter = mCarCropAdapter
    }

    override fun initListener() {
        layout_back.setOnClickListener { NavigationHelper.instance?.backToHandleJD(this@VehicleRecordFragment) }
        recycler_cropimg_view.addOnItemTouchListener(RecyclerViewMultiClickListener(context, recycler_cropimg_view, object : SimpleOnItemClickListener() {
            override fun onItemClick(view: View, position: Int) {
                val url = Util.getCarPhotoIpHead() + mCarCropAdapter!!.mDateData[position].url
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        }))
        text_unrecog.setOnClickListener {
            text_unrecog.setBackgroundResource(R.drawable.car_switch_bg)
            text_recog.background = null
            vehicle_list_recog.visibility = View.GONE
            vehicle_list_notrecog.visibility = View.VISIBLE
            if (mVehicleNotInLibList.size > 0) {
                mCarCropAdapter!!.setAdapter(mVehicleNotInLibList, mCurrentDate)
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
        text_recog.setOnClickListener {
            text_unrecog.background = null
            text_recog.setBackgroundResource(R.drawable.car_switch_bg)
            vehicle_list_notrecog.visibility = View.GONE
            vehicle_list_recog.visibility = View.VISIBLE
            if (mVehicleInfoList.size > 0) {
                mVehicleListAdapter!!.setAdapter(mVehicleInfoList)
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
        recycler_view_car.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE &&
                        firstVisibleItem == 0) {
                    fetchCarRecord()
                    return
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition()
                }
            }
        })
        initObserver()
    }

    override fun onBackPressed() {
        NavigationHelper.instance.backToHandleJD(this)
//        return true
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VehicleRecordFragment.
         */
        // TODO: Rename and change types and number of parameters
        fun newInstance(param1: String?, param2: String?): VehicleRecordFragment {
            val fragment = VehicleRecordFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }
}