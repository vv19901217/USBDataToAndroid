package cn.wiky.serial_port;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by wiky_zhang on 2018/1/15.
 */

public class DataAdapter extends BaseAdapter {

    private List<String> dataList;
    private Context context;

    public DataAdapter(List<String> dataList, Context context) {
        this.dataList = dataList;
        this.context = context;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        Holder holder=null;
        if (view==null
                ){
            view= LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1,viewGroup,false);
            holder=new Holder();
            holder.textview=view.findViewById(android.R.id.text1);
            view.setTag(holder);
        }
        holder= (Holder) view.getTag();
        holder.textview.setText(dataList.get(i));
        return view;
    }

    class Holder{
        TextView textview;
    }

    public void refresh(List<String> dataList){
        this.dataList=dataList;
        notifyDataSetChanged();
    }
}
