package com.andrsay.oreonotificationsample;

import android.app.NotificationChannel;
import android.app.NotificationChannelGroup;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhanglei on 2018/1/6.
 */

public class Demo {
    public static final int aa = 1, bb = 2;

    @IntDef({aa, bb})
    @interface CC{

    }


    class LAdapter extends RecyclerView.Adapter<LAdapter.ViewHolder>{

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            List<String> list = new ArrayList<>();
            list.add("");
            int a = true ? 1 : 2;
            return null;
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {

        }

        @Override
        public int getItemCount() {
            return 0;
        }

        class ViewHolder extends RecyclerView.ViewHolder{

            public ViewHolder(View itemView) {
                super(itemView);
                new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        dialog.cancel();
                    }
                };
            }
        }

        class AA extends ArrayAdapter<NotificationChannelGroup>{

            public AA(@NonNull Context context, int resource) {
                super(context, resource);
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                });
                return super.getDropDownView(position, convertView, parent);
            }
        }
    }

    interface OnChannelOperateCallback{
        void onSave(NotificationChannel channel);
        void onDelete(NotificationChannel channel);
    }
}
