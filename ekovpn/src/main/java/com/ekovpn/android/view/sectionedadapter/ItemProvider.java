package com.ekovpn.android.view.sectionedadapter;


interface ItemProvider {

  int getSectionCount();

  int getItemCount(int sectionIndex);

  boolean showHeadersForEmptySections();

  boolean showFooters();
}
