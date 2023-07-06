package sklookie.bowwow.community

import android.os.Bundle
import android.view.Menu
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import sklookie.bowwow.R
import sklookie.bowwow.databinding.ActivityNavigateBinding

private const val TAG_COMMUNITY = "community_fragment"

class NavigateActivity : AppCompatActivity() {
    private lateinit var binding : ActivityNavigateBinding

    val fragmentManager = supportFragmentManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNavigateBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.navigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.community_menu -> setFragment(TAG_COMMUNITY, CommunityFragment())
            }
            true
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val fragment = fragmentManager.findFragmentById(R.id.mainFrameLayout)

        if (fragment is PostFragment) {
            menuInflater.inflate(R.menu.menu_extra, menu)
            return super.onCreateOptionsMenu(menu)
        }

        return super.onCreateOptionsMenu(menu)
    }

    private fun setFragment(tag: String, fragment: Fragment) {
        val fragmentTransaction = fragmentManager.beginTransaction()

        if (fragmentManager.findFragmentByTag(tag) == null) {
            fragmentTransaction.add(R.id.mainFrameLayout, fragment, tag)
        }

        val community = fragmentManager.findFragmentByTag(TAG_COMMUNITY)

        if (community != null) {
            fragmentTransaction.hide(community)
        }

        if (tag.equals(TAG_COMMUNITY)) {
            if (community != null) {
                fragmentTransaction.show(community)
            }
        }

        fragmentTransaction.commitAllowingStateLoss()
    }

    override fun onBackPressed() {
        val fragment = fragmentManager.findFragmentById(R.id.mainFrameLayout)

        if (fragment is PostFragment) {
            fragmentManager.popBackStack()
        } else {
            return
        }
    }
}