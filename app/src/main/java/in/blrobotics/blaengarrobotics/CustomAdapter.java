package in.blrobotics.blaengarrobotics;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.*;

public class CustomAdapter extends ArrayAdapter implements  View.OnClickListener {
    int resource;
    List<Integer> positionToId;
    Class<?> onClickCls;

    public CustomAdapter(Context context, int resource,Class<?> onClickCls, List<List> dataset) {
        super(context, resource, dataset);
        this.resource = resource;
        this.onClickCls = onClickCls;
    }

    public CustomAdapter(Context context, int resource,Class<?> onClickCls,List<List> dataset,List<Integer> positionToId) {
        super(context, resource, dataset);
        this.resource = resource;
        this.positionToId = positionToId;
        this.onClickCls = onClickCls;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        //Inflating the layout
        LayoutInflater layoutInflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        convertView = layoutInflater.inflate(resource, parent, false);
        convertView.setOnClickListener(this);

        if (positionToId != null){
            convertView.setTag(positionToId.get(position));
        }
        else{
            convertView.setTag(position);
        }

        //Get the references to the view objects and set value
        int i=0;
        for(View view:viewGroupToViewList((ViewGroup)convertView)){
            switch (view.getClass().getSimpleName()) {
                case "AppCompatImageView":
                    ImageView imageView = (ImageView) view;
                    imageView.setImageResource(Integer.parseInt(((List<String>)getItem(position)).get(i++)));
                    break;
                case "AppCompatTextView":
                    TextView textView = (TextView) view;
                    if(textView.getText().toString().isEmpty()){
                        textView.setText(((List<String>)getItem(position)).get(i++));
                    }
                    break;
            }
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        int id = (Integer) v.getTag();
        Toast toast = Toast.makeText(getContext(), String.valueOf(id), Toast.LENGTH_SHORT);
        toast.show();
        Intent intent = new Intent(getContext(),onClickCls);
        intent.putExtra("deviceId",id);
        getContext().startActivity(intent);
    }

    public List<View> viewGroupToViewList(ViewGroup parent) {
        List<View> viewList = new ArrayList<>();
        for (int i = 0; i < parent.getChildCount(); i++) {
            View child = parent.getChildAt(i);
            switch (child.getClass().getSimpleName()) {
                case "AppCompatImageView":
                    viewList.add(child);
                    break;
                case "AppCompatTextView":
                    viewList.add(child);
                    break;
                default:
                    viewList.addAll(viewGroupToViewList((ViewGroup) child));
                    break;
            }

        }
        return viewList;
    }
}
