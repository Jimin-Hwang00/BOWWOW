package sklookie.bowwow

import android.app.ActivityManager
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import sklookie.bowwow.bluetooth.BluetoothFragment
import sklookie.bowwow.community.AddFragment
import sklookie.bowwow.community.CommunityFragment
import sklookie.bowwow.community.EditFragment
import sklookie.bowwow.community.PostFragment
import sklookie.bowwow.community.TAG_COMMUNITY
import sklookie.bowwow.databinding.ActivityNavigateBinding


class NavigateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNavigateBinding

    val fragmentManager = supportFragmentManager

    lateinit var id: String

    lateinit var currentTag: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        id = intent.getStringExtra("id").toString()

        binding.navigationView.run { setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.home_bottom_menu -> {
                    val fragment = MainHomeFragment()
                    val bundle = Bundle()
                    bundle.putString("key", id.toString()) // 데이터 추가
                    fragment.arguments = bundle

                    supportFragmentManager.beginTransaction()
                        .replace(R.id.mainFrameLayout, fragment)
                        .commit()
                }
                R.id.community_bottom_menu -> {
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, CommunityFragment()).commit()
                }
                R.id.bluetooth_bottom_menu->{
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, BluetoothFragment()).commit()
                }
                R.id.myInfo_bottom_menu->{
                    supportFragmentManager.beginTransaction().replace(R.id.mainFrameLayout, MyInfoFragment()).commit()
                }
            }
            true
        }
            selectedItemId = R.id.home_bottom_menu
        }
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()
        val existingFragment = fragmentManager.findFragmentByTag(tag)

        currentTag = tag

        Log.d("NavigateActivity","setFragment 실행")

        if (existingFragment == null) {
            fragment.arguments = Bundle().apply {
                putString("id", id)
            }
            fragmentTransaction.add(R.id.mainFrameLayout, fragment, tag)
        } else {
            fragmentTransaction.show(existingFragment)
        }

        val menu = fragmentManager.findFragmentByTag(TAG_MAINHOME)
        val community = fragmentManager.findFragmentByTag(TAG_COMMUNITY)

        if (menu != null && menu !== existingFragment) {
            fragmentTransaction.hide(menu)
        }
        if (community != null && community !== existingFragment) {
            fragmentTransaction.hide(community)
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val fragment = fragmentManager.findFragmentById(R.id.mainFrameLayout)

        if (fragment is PostFragment || fragment is AddFragment || fragment is EditFragment) {
            fragmentManager.popBackStack()
        } else {
            return
        }

        val stackCount = supportFragmentManager.backStackEntryCount
        if (stackCount == 1) {
            return
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        finishAffinity()
    }
}