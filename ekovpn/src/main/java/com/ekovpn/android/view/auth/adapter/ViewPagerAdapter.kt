/*
 * Copyright (c) 2012-2020 Abdul-Mujeeb Aliu for ekoVPN
 * Distributed under the GNU GPL v2 with additional terms. For full terms see the file doc/LICENSE.txt
 */

package com.ekovpn.android.view.auth.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.ekovpn.android.view.auth.login.LoginFragment
import com.ekovpn.android.view.auth.success.SuccessFragment


class ViewPagerAdapter(private val fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    override fun createFragment(position: Int): Fragment {
        return if(position == 0){
            LoginFragment()
        }else{
            SuccessFragment()
        }
    }

    override fun getItemCount(): Int {
        return SIZE
    }

    companion object {
        private const val SIZE = 2
    }
}