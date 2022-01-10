package com.hiar.ar110.helper

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.hiar.ar110.ConstantApp
import com.hiar.ar110.R
import com.hiar.ar110.base.BaseFragment
import com.hiar.ar110.data.LocationData
import com.hiar.ar110.data.cop.CopTaskRecord
import com.hiar.ar110.data.cop.PatrolRecord
import com.hiar.ar110.data.people.PopulationRecord
import com.hiar.ar110.fragment.*
import com.hiar.ar110.util.Util
import com.hiar.mybaselib.utils.AR110Log

/**
 *
 * @author xuchengtang
 * @date 7/06/2021
 * Email: xucheng.tang@hiscene.com
 */
class NavigationHelper private constructor() {
    private var activity: FragmentActivity? = null

    companion object {
        val TAG = this.javaClass.simpleName
        var TAG_COP_TASK = "TAG_COP_TASK"
        var TAG_PEOPLE_RECORD: String = "TAG_PEOPLE_RECORD"
        var TAG_HOME_PAGE: String = "TAG_HOME_PAGE"
        var TAG_SETTINGS: String = "TAG_SETTINGS"
        var TAG_JD_LIST: String = "TAG_JD_LIST"
        var TAG_PATROL_LIST: String = "TAG_PATROL_LIST"
        var TAG_POPULATION: String = "TAG_POPULATION"
        var TAG_JD_HANDLE: String = "TAG_JD_HANDLE"
        var TAG_RECENT_MSG: String = "TAG_RECENT_MSG"
        var TAG_POPULATION_HANDLE: String = "TAG_JD_HANDLE"
        val instance: NavigationHelper by lazy(mode = LazyThreadSafetyMode.SYNCHRONIZED) {
            NavigationHelper() }
    }

    fun init(fragmentActivity: FragmentActivity?) {
        activity = fragmentActivity
    }
    private fun getFragments():List<Fragment> {
       return activity!!.supportFragmentManager.fragments
    }

    fun onHileiaComingCallCallback() {
        var len=getFragments().size
        if (len > 0)
            for (i in 0 until  len) {
                AR110Log.i(TAG, "onHileiaComingCallCallback:$i %s", getFragments()[i].javaClass.name)
                (getFragments()[i] as BaseFragment).onHileiaComingCallCallback()
            }
    }

//    fun onHileiaComingCallCallback() {
//        mFG?.onHileiaComingCallCallback()
//    }

//    fun onSubFGHileiaHangupCallback() {
//        mSubFG?.onHileiaHangupCallback()
//    }

    fun onHileiaHangupCallback() {
        var len=getFragments().size
        if (len > 0)
            for (i in 0 until  len) {
                AR110Log.i(TAG, "onHileiaHangupCallback:$i %s", getFragments()[i].javaClass.name)
                (getFragments()[i] as BaseFragment).onHileiaHangupCallback()
            }
//        mFG?.onHileiaHangupCallback()
    }

//    private fun onSubFGBackPressed(): Boolean {
//        return if (mSubFG != null) {
//            mSubFG!!.onBackPressed()
//        } else false
//    }

    fun updateGpsLocation(locationData: LocationData?) {
//        mFG?.updateGpsLocation(locationData!!)
        var len = getFragments().size
        if (len > 0)
            for (i in 0 until len) {
                AR110Log.i(TAG, "updateGpsLocation:$i %s",getFragments()[i].javaClass.name)
                (getFragments()[i] as BaseFragment).updateGpsLocation(locationData!!)
            }
    }

    /**
     * 进入图片记录界面
     */
    fun beginShowPhotoRecord(task: CopTaskRecord?, patrolTask: PatrolRecord?) {
        if (null == task && patrolTask == null) {
            return
        }
        val mPhotoUploadNewFragment: BaseFragment = PhotoUploadNewFragment()
        val bundle = Bundle()
        if (null != task) {
            bundle.putParcelable("key_cop_task", task)
        } else if (patrolTask != null) {
            bundle.putParcelable("key_patrol_task", patrolTask)
        }
        mPhotoUploadNewFragment.arguments = bundle
        addFragment(mPhotoUploadNewFragment, "photo_record")
    }

    /**
     * 进入车辆记录界面
     */
    fun beginShowCarRecord(task: CopTaskRecord?, pTask: PatrolRecord?) {
        val mVehicleRecordFragment: BaseFragment = VehicleRecordFragment()
        val bundle = Bundle()
        if (null != task) {
            bundle.putParcelable("key_cop_task", task)
        }
        if (null != pTask) {
            bundle.putParcelable("key_patrol_task", pTask)
        }
        mVehicleRecordFragment.arguments = bundle
        addFragment(mVehicleRecordFragment, "vehicle_record")
    }

    /**
     * 进入人员识别记录界面
     */
    fun beginShowPeopleRecord(task: CopTaskRecord?, patrolRecord: PatrolRecord?) {
        val mPeopleRecordFragment: BaseFragment = PeopleResultFragment()
        val bundle = Bundle()
        if (null != task) {
            bundle.putParcelable("key_cop_task", task)
        } else if (patrolRecord != null) {
            bundle.putParcelable("key_patrol_task", patrolRecord)
        }
        mPeopleRecordFragment.arguments = bundle
        addFragment(mPeopleRecordFragment, TAG_PEOPLE_RECORD)
    }

    /**
     * 进入视频记录界面
     */
    fun beginVideoUpload(task: CopTaskRecord?, patrolTask: PatrolRecord?) {
        val mCopTaskFragment: BaseFragment = VideoListFragment()
        val bundle = Bundle()
        if (null != task) {
            bundle.putParcelable("key_cop_task", task)
        } else if (null != patrolTask) {
            bundle.putParcelable("key_patrol_task", patrolTask)
        }
        mCopTaskFragment.arguments = bundle
        addFragment(mCopTaskFragment,TAG_COP_TASK)
    }

    /**
     * 返回主界面
     */
    fun backToHomePage(fragment: BaseFragment?) {
        val mHomePageFragment: BaseFragment = HomePageFragment()
        if (fragment != null) {
            val transaction = activity!!.supportFragmentManager.beginTransaction()
            transaction.remove(fragment)
        }
        replaceFragment(mHomePageFragment,TAG_HOME_PAGE)
    }

    /**
     * 进入设置界面
     */
    fun beginSettingsFragment() {
        val mSettingsFragment: BaseFragment = SettingsFragment()
        replaceFragment(mSettingsFragment,TAG_SETTINGS)
    }

    /**
     * 第一次进入主界面
     */
    fun beginHomePage() {
        val mHomePageFragment: BaseFragment = HomePageFragment()
        replaceFragment(mHomePageFragment,TAG_HOME_PAGE)
    }

    /**
     * 进入警单列表界面
     */
    fun backToJdList(checkNewJd: Boolean) {
        if (!checkNewJd) {
            Util.mNewMsgCjdbh2 = null
        }
        val mJdFragment: BaseFragment = JDListFragment()
        replaceFragment(mJdFragment,TAG_JD_LIST)
    }

    /**
     * 进入巡逻列表界面
     */
    fun backToPatrolList() {
        val mPatrolFragment: BaseFragment = PatrolListFragment()
        replaceFragment(mPatrolFragment,TAG_PATROL_LIST)
    }

    private fun replaceFragment(fragment: BaseFragment,tag:String) {
        // 清除界面上的复制框
        activity?.window?.decorView?.clearFocus()
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.replace(R.id.fragment_container, fragment,tag)
        transaction.commit()
//        mFG = fragment
//        mSubFG = null
    }

    private fun addFragment(fragment: BaseFragment, tag: String) {
        addFragment(fragment, null, tag)
    }

    private fun addFragment(fragment: BaseFragment, lastFragment: BaseFragment?, tag: String) {
        // 清除界面上的复制框
        activity?.window?.decorView?.clearFocus()
        val fragmentManager = activity!!.supportFragmentManager
        val transaction = fragmentManager.beginTransaction()
        if (lastFragment != null && lastFragment.isAdded) {
            transaction.remove(lastFragment)
        }
        if (!fragment.isAdded && fragmentManager.findFragmentByTag(tag) == null) {
            transaction.add(R.id.fragment_container, fragment, tag)
        }
        if (fragment.isAdded) {
            transaction.show(fragment)
        }
        transaction.commit()
//        mSubFG = fragment
    }

    /**
     * 进入流动人口核查界面
     */
    fun backPopulationVerification() {
        val mpopulationFragment: BaseFragment = PopulationFragment()
        replaceFragment(mpopulationFragment,TAG_POPULATION)
    }

    /**
     * 从其他历史记录界面，返回到出警主界面
     */
    fun backToHandleJD(fragment: BaseFragment?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.remove(fragment!!)
        transaction.commit()

        val mJDHandleFragment = instance.findFragment(TAG_JD_HANDLE)
        if (mJDHandleFragment != null && mJDHandleFragment is JDHandleFragment) {
            mJDHandleFragment.userVisibleHint=true
        }
//        mFG = activity!!.supportFragmentManager.fragments[0] as BaseFragment
//        mSubFG = null
    }

    /**
     * 进入出警主界面
     */
    fun beginHandleJD(task: CopTaskRecord?, patrolTask: PatrolRecord?) {
        if (null == task && patrolTask == null) {
            return
        }
        val mJDHandleFragment: BaseFragment = JDHandleFragment()
        val bundle = Bundle()
        if (task != null) {
            bundle.putParcelable("key_cop_task", task)
        } else if (patrolTask != null) {
            bundle.putParcelable("key_patrol_task", patrolTask)
        }
        mJDHandleFragment.arguments = bundle
        replaceFragment(mJDHandleFragment,TAG_JD_HANDLE)
    }

    /**
     * 进入消息列表页
     */
    fun beginRecentMsg() {
        val mRecentMsgFragment: BaseFragment = RecentMsgFragment()
        replaceFragment(mRecentMsgFragment,TAG_RECENT_MSG)
    }

    fun exitChat(fragment: BaseFragment?) {
        val transaction = activity!!.supportFragmentManager.beginTransaction()
        transaction.remove(fragment!!)
        transaction.commit()

        val mJDHandleFragment = instance.findFragment(TAG_JD_HANDLE)
        if (mJDHandleFragment != null && mJDHandleFragment is JDHandleFragment) {
            mJDHandleFragment.userVisibleHint=true
        }
//        mFG = activity!!.supportFragmentManager.fragments[0] as BaseFragment
//        AR110Log.i(TAG, "exitChat: %s", mFG!!.javaClass.name)
//        mSubFG = null
//        printFragments()
    }

    /**
     * 进入聊天界面
     */
    fun beginChat(arguments: Bundle?, fragment: BaseFragment?) {
        val mChatFragment: BaseFragment = ChatFragment()
        mChatFragment.arguments = arguments
        addFragment(mChatFragment, fragment, "chat")
    }

    /**
     * 进入群组详情界面
     */
    fun beginGroupDetail(groupId: String?, fragment: BaseFragment?) {
        val mGroupDetailFragment: BaseFragment = GroupDetailFragment()
        val bundle = Bundle()
        bundle.putString(ConstantApp.CHAT_GROUP_ID_KEY, groupId)
        mGroupDetailFragment.arguments = bundle
        addFragment(mGroupDetailFragment, fragment, "group_detail")
    }

    /**
     * 进入添加群成员界面
     */
    fun beginGroupManager(groupId: String?, fragment: BaseFragment?) {
        val mGroupManagerFragment: BaseFragment = GroupManagerFragment()
        val bundle = Bundle()
        bundle.putString(ConstantApp.CHAT_GROUP_ID_KEY, groupId)
        mGroupManagerFragment.arguments = bundle
        addFragment(mGroupManagerFragment, fragment, "group_manager")
    }

    /**
     * 进入音频记录界面
     */
    fun beginShowAudioRecord(task: CopTaskRecord?, patrolTask: PatrolRecord?) {
        if (null == task && patrolTask == null) {
            return
        }
        val mAudioUploadFragment: BaseFragment = AudioUploadFragment()
        val bundle = Bundle()
        if (null != task) {
            bundle.putParcelable("key_cop_task", task)
        } else if (patrolTask != null) {
            bundle.putParcelable("key_patrol_task", patrolTask)
        }
        mAudioUploadFragment.arguments = bundle
        addFragment(mAudioUploadFragment, "audio_record")
    }

    /**
     * 进入人口核查主界面
     */
    fun beginHandlePopulation(task: PopulationRecord?) {
        if (null == task) {
            return
        }
        val mJDHandleFragment: BaseFragment = PopulationResultFragment()
        val bundle = Bundle()
        bundle.putParcelable("key_population_task", task)
        mJDHandleFragment.arguments = bundle
        replaceFragment(mJDHandleFragment,TAG_POPULATION_HANDLE)
    }

//    val isJDListFragment: Boolean
//        get() = mFG != null && mFG is JDListFragment
//    val isHomePageFragment: Boolean
//        get() = mFG != null && mFG is HomePageFragment

    fun findFragment(tag:String): Fragment? {
        return activity!!.supportFragmentManager.findFragmentByTag(tag)
    }

    fun makeActivityToBG() {
        activity?.moveTaskToBack(true)
    }
    //交给各fragment 自己处理
    fun onBackPressed() {
        if(getFragments()!=null && getFragments().isNotEmpty()){
            AR110Log.i(TAG, "onBackPressed: %s",(getFragments()[getFragments().size-1]).javaClass.name)
            (getFragments().get(getFragments().size-1) as BaseFragment).onBackPressed()
        }
//        onSubFGHileiaHangupCallback() //TODO 为什么就子页面需要调用隐藏

//        if (!ret) {
//            if (mFG is HomePageFragment) {
//                makeActivityToBG()
//                return
//            }
//            if (mFG is SettingsFragment) {
//                backToHomePage(mFG)
//                return
//            }
//            if (mFG is JDListFragment) {
//                backToHomePage(mFG)
//                return
//            }
//            if (mFG is PatrolListFragment) {
//                backToHomePage(mFG)
//            }
//        }
    }

    fun onDestroy() {
//        mFG = null
//        mSubFG = null

    }



}