package com.hiar.ar110.fragment

import android.app.Activity
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.hiar.ar110.R
import com.hiar.ar110.activity.AR110MainActivity
import com.hiar.ar110.activity.AR110MainActivity.Companion.isPushing
import com.hiar.ar110.adapter.PeopleCropImgAdapter
import com.hiar.ar110.adapter.PeopleDateListAdapter
import com.hiar.ar110.adapter.PeopleListAdapter
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.people.FaceCompareBaseInfo
import com.hiar.ar110.data.people.PeopleHisResult
import com.hiar.ar110.helper.NavigationHelper.Companion.instance
import com.hiar.ar110.listener.OnImageItemClickListener
import com.hiar.ar110.util.Util
import com.hiar.ar110.viewmodel.PeopleResultViewModel
import com.hiar.ar110.widget.RecyclerViewMultiClickListener
import com.hiar.ar110.widget.RecyclerViewMultiClickListener.SimpleOnItemClickListener
import com.hiar.ar110.widget.SpaceDecoration
import com.hiar.mybaselib.utils.AR110Log
import kotlinx.android.synthetic.main.people_result.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

/**
 *
 * @author xuchengtang
 * @date 11/06/2021
 * Email: xucheng.tang@hiscene.com
 */
class PeopleResultFragment : BaseFragment() {
    private var mCopTask: CopTaskRecord? = null
    private var mPatrolRecord: PatrolRecord? = null
    private var mPeopleListAdapter: PeopleListAdapter? = null
    private var mDateListAdapter: PeopleDateListAdapter? = null
    private var mPeopleCropAdapter: PeopleCropImgAdapter? = null
    private val mPeopleInfoList = ArrayList<Array<FaceCompareBaseInfo>>()
    private val mPeopleNotInLibMap: MutableMap<String, Array<FaceCompareBaseInfo>> = HashMap()
    private var mTotalItem = 0
    private var mNotInLibNum = 0
    private var mInLibNum = 0
    private var mCurrentDate: String? = null
    private val mDateList: ArrayList<String?> = ArrayList()
    private var mUnRecogArray: Array<FaceCompareBaseInfo>? = null
    private var mPeoResult: PeopleHisResult.FaceRecogHisData? = null
    fun setNewDate(curdate: String?) {
        recycler_view_datelist!!.visibility = View.GONE
        img_drop_icon!!.setImageResource(R.drawable.ic_icon_up)
        val faceList = ArrayList<FaceCompareBaseInfo>()
        val index = mDateList.indexOf(curdate)
        mUnRecogArray = mPeoResult!!.unrecoFace[index]
        if (null != mUnRecogArray && mUnRecogArray!!.isNotEmpty()) {
            mCurrentDate = curdate
            val dateArray = mCurrentDate!!.split("-").toTypedArray()
            val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
            text_date_sel!!.text = dateDisplay
            val len = mUnRecogArray!!.size
            for (i in 0 until len) {
                if (mUnRecogArray!![i].gpsTime.contains(curdate!!)) {
                    faceList.add(mUnRecogArray!![i])
                }
            }
            mPeopleCropAdapter!!.setAdapter(faceList)
        }
    }

    override fun onStart() {
        super.onStart()
        if (isPushing) {
            layout_commu_phone!!.visibility = View.VISIBLE
        }
        with(mViewModel) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    fetchFaceHiscene(mCopTask,mPatrolRecord)
                }
            }
        }
    }

    fun fetchFaceHiscene() {
        with(mViewModel) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    fetchFaceHiscene(mCopTask,mPatrolRecord)
                }
            }
        }
    }

    private var firstVisibleItem: Long = 0
    override fun getLayoutId(): Int {
        return R.layout.people_result
    }

    override fun initData() {
        if (arguments != null) {
            mCopTask = arguments!!.getParcelable("key_cop_task")
            mPatrolRecord = arguments!!.getParcelable("key_patrol_task")
        }
    }

    override fun initView(contentView: View) {
        mPeopleListAdapter = PeopleListAdapter(object : OnImageItemClickListener {
            override fun onItemClick(url: String) {
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        })
        val mLinearLayoutManager = LinearLayoutManager(activity)
        recycler_view_peopleinfo.layoutManager = mLinearLayoutManager
        recycler_view_peopleinfo.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp16).toInt()))
        recycler_view_peopleinfo.adapter = mPeopleListAdapter
        img_drop_icon.visibility = View.INVISIBLE
        recycler_cropimg_view.addItemDecoration(SpaceDecoration(resources.getDimension(R.dimen.dp8).toInt(), resources.getDimension(R.dimen.dp4).toInt()))
        mPeopleCropAdapter = PeopleCropImgAdapter(contentView.context.applicationContext)
        recycler_cropimg_view.adapter = mPeopleCropAdapter
        mDateListAdapter = PeopleDateListAdapter(this)
        val mLinearLayoutManagerDate = LinearLayoutManager(activity)
        recycler_view_datelist.layoutManager = mLinearLayoutManagerDate
        recycler_view_datelist.addItemDecoration(SpaceDecoration(0, resources.getDimension(R.dimen.dp1).toInt()))
        recycler_view_datelist.adapter = mDateListAdapter
    }

    override fun initListener() {
        recycler_cropimg_view.addOnItemTouchListener(RecyclerViewMultiClickListener(context, recycler_cropimg_view, object : SimpleOnItemClickListener() {
            override fun onItemClick(view: View, position: Int) {
                val url = Util.getPeoplePhotoIpHead() + mPeopleCropAdapter!!.mListData[position].name
                (activity as AR110MainActivity?)!!.showLargeImg(url)
            }
        }))
        recycler_view_peopleinfo!!.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE && firstVisibleItem == 0L) {
                    fetchFaceHiscene()
                    return
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                val layoutManager = recyclerView.layoutManager
                if (layoutManager is LinearLayoutManager) {
                    firstVisibleItem = layoutManager.findFirstVisibleItemPosition().toLong()
                }
            }
        })
        layout_back.setOnClickListener { instance.backToHandleJD(this@PeopleResultFragment) }
        text_date_sel.setOnClickListener { img_drop_icon!!.performClick() }
        text_unrecog.setOnClickListener {
            text_unrecog.setBackgroundResource(R.drawable.car_switch_bg)
            text_recog.background = null
            people_list_recog!!.visibility = View.GONE
            people_list_notrecog!!.visibility = View.VISIBLE
            if (!TextUtils.isEmpty(mCurrentDate)) {
                setNewDate(mCurrentDate)
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }
        text_recog!!.setOnClickListener {
            text_unrecog.background = null
            text_recog.setBackgroundResource(R.drawable.car_switch_bg)
            people_list_notrecog!!.visibility = View.GONE
            people_list_recog!!.visibility = View.VISIBLE
            if (mPeopleInfoList.size > 0) {
                mPeopleListAdapter!!.setAdapter(mPeopleInfoList)
            }
            (activity as AR110MainActivity?)!!.hideLargeImg()
        }

        mViewModel.peopleHisResult.observe(this, androidx.lifecycle.Observer{
            AR110Log.d(TAG, "facelist $it")
            val peoRes: PeopleHisResult.FaceRecogHisData? = it
            mPeopleInfoList.clear()
            mPeopleNotInLibMap.clear()
            mDateList.clear()
            if (peoRes != null) {
                mPeoResult = it
                Util.mFacePhotoUrlHead = peoRes.imageUrl
                if (peoRes.recoFace != null && peoRes.recoFace.isNotEmpty()) {
                    for (i in peoRes.recoFace.indices) {
                        mPeopleInfoList.add(peoRes.recoFace[i])
                    }
                } else {
                    mInLibNum = 0
                }
                if (peoRes.unrecoFace != null && peoRes.unrecoFace.isNotEmpty()) {
                    mNotInLibNum = 0
                    mPeopleNotInLibMap.clear()
                    mDateList.clear()
                    val dateLen = peoRes.unrecoFace.size
                    for (j in 0 until dateLen) {
                        mUnRecogArray = peoRes.unrecoFace[j]
                        val itemLen = mUnRecogArray!!.size
                        mCurrentDate = ""
                        val lastTime = ""
                        for (i in 0 until itemLen) {
                            val face = mUnRecogArray!![i]
                            var gpsTime = face.gpsTime
                            gpsTime = gpsTime.substring(0, "2021-01-27".length)
                            if (!TextUtils.isEmpty(face.gpsTime)) {
                                if (!mDateList.contains(gpsTime)) {
                                    mDateList.add(0, gpsTime)
                                }
                                mNotInLibNum++
                            }
                        }
                    }
                    mDateList.sortWith { o1, o2 -> -o1!!.compareTo(o2!!) }
                    mCurrentDate = if (mDateList.size > 0) {
                        mDateList[0]
                    } else {
                        ""
                    }
                } else {
                    mNotInLibNum = 0
                    mCurrentDate = ""
                }
                mInLibNum = mPeopleInfoList.size

                mTotalItem = mNotInLibNum + mInLibNum
                val title = getString(R.string.people_inspect_title)
                val strInLib = getString(R.string.vehicle_has_recog) + " " + String.format(" (%d)", mInLibNum)
                text_recog.text = strInLib
                text_title.text = title
                mPeopleListAdapter?.setAdapter(mPeopleInfoList)
                if (!TextUtils.isEmpty(mCurrentDate)) {
                    val dateArray = mCurrentDate!!.split("-").toTypedArray()
                    val dateDisplay = String.format("%s年%s月%s日", dateArray[0], dateArray[1], dateArray[2])
                    text_date_sel!!.text = dateDisplay
                    if (mNotInLibNum > 0) {
                        img_drop_icon.visibility = View.VISIBLE
                    } else {
                        img_drop_icon.visibility = View.INVISIBLE
                    }
                    img_drop_icon.isClickable = true
                    img_drop_icon.setOnClickListener {
                        if (recycler_view_datelist.visibility == View.VISIBLE) {
                            recycler_view_datelist.visibility = View.INVISIBLE
                            img_drop_icon.setImageResource(R.drawable.ic_icon_up)
                        } else {
                            mDateListAdapter?.setAdapter(mDateList)
                            recycler_view_datelist.visibility = View.VISIBLE
                            img_drop_icon.setImageResource(R.drawable.ic_icon_down)
                        }
                    }
                }
                text_recog.isClickable = true
                text_unrecog.isClickable = true
            } else {
                mPeopleInfoList.clear()
                mPeopleNotInLibMap.clear()
                mDateList.clear()

                mPeopleListAdapter?.resetAdapter()
                mDateListAdapter?.resetAdapter()
                mPeopleCropAdapter?.resetAdapter()
            }

        })
    }
    private val mViewModel by lazy {
        getViewModel(PeopleResultViewModel::class.java)
    }

    override fun onBackPressed() {
        instance.backToHandleJD(this@PeopleResultFragment)
        //        return true;
    }

    override fun onHileiaComingCallCallback() {
        val act: Activity? = activity
        act?.runOnUiThread { layout_commu_phone!!.visibility = View.VISIBLE }
    }

    override fun onHileiaHangupCallback() {
        val act: Activity? = activity
        act?.runOnUiThread { layout_commu_phone!!.visibility = View.GONE }
    }
}