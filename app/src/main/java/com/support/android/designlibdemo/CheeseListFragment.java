/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.support.android.designlibdemo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class CheeseListFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
      savedInstanceState) {
    RecyclerView rv = (RecyclerView) inflater.inflate(
        R.layout.fragment_cheese_list, container, false);
    setupRecyclerView(rv);
    return rv;
  }

  @Override
  public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
    Log.d("test", "onViewCreated");
    super.onViewCreated(view, savedInstanceState);
  }

  @Override
  public void onActivityCreated(@Nullable Bundle savedInstanceState) {
    Log.d("test", "onActivityCreated");
    super.onActivityCreated(savedInstanceState);
  }

  private void setupRecyclerView(RecyclerView recyclerView) {
    recyclerView.setLayoutManager(new LinearLayoutManager(recyclerView.getContext()));
    recyclerView.setAdapter(new SimpleStringRecyclerViewAdapter(getActivity(),
        DemoUtils.getRandomSublist(Cheeses.sCheeseStrings, 30)) {
      @Override
      public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        holder.mView.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
            Context context = v.getContext();
//            Intent intent = new Intent(context, EmbedRecyclerViewDemoActivity.class);
//            intent.putExtra("mode", getArguments().getString("mode"));
//            intent.putExtra(CheeseDetailActivity.EXTRA_NAME, holder.mBoundString);
//            context.startActivity(intent);
            Intent intent = new Intent(context, MultiExpandableListDemoActivity.class);
            context.startActivity(intent);
          }
        });
      }
    });
  }


}
