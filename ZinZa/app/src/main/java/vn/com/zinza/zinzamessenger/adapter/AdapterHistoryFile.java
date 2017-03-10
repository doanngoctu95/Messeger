package vn.com.zinza.zinzamessenger.adapter;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Locale;

import vn.com.zinza.zinzamessenger.R;
import vn.com.zinza.zinzamessenger.model.FileHistory;

/**
 * Created by dell on 17/02/2017.
 */

public class AdapterHistoryFile  extends ArrayAdapter<FileHistory> {
    private ArrayList<FileHistory> arrFile;
    private ArrayList<FileHistory> searchList;
    private LayoutInflater inflater;
    private Context context;

    public AdapterHistoryFile(Context mContext, int resource, ArrayList<FileHistory> objects) {
        super(mContext, resource, objects);
        context=mContext;
        arrFile= objects;
        inflater= (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        searchList= new ArrayList<>();
        searchList.addAll(arrFile);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;

        if (convertView==null){
            viewHolder= new ViewHolder();
            convertView= inflater.inflate(R.layout.item_history_file,parent,false);

            ImageView imageView= (ImageView) convertView.findViewById(R.id.imFile);
            TextView tvName= (TextView) convertView.findViewById(R.id.tvNameFile);
            TextView tvDate= (TextView) convertView.findViewById(R.id.tvTimeFile);
            ImageView imgOption= (ImageView) convertView.findViewById(R.id.imgOption);

            viewHolder.img=imageView;
            viewHolder.tvName=tvName;
            viewHolder.tvDate=tvDate;
            viewHolder.imgOption=imgOption;

            convertView.setTag(viewHolder);
        }

        viewHolder= (ViewHolder) convertView.getTag();
//        viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(arrFile.get(position).getImg()));
        String typeFile= arrFile.get(position).getName();
        if (typeFile.contains(".html")){
//            viewHolder.img.setImageDrawable(R.drawable.);
        }
        else if (typeFile.contains(".xls")){
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.exel_icon));
        }
        else if (typeFile.contains(".doc")){
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.word_icon));
        }
        else if (typeFile.contains(".zip")||typeFile.contains(".rar")){
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.rar));
        }
        else if (typeFile.contains(".ppt")){
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.ppt_icon));
        }
        else if (typeFile.contains(".pdf")){
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.pdf_icon));
        }
        else {
            // other file
            viewHolder.img.setImageDrawable(getContext().getResources().getDrawable(R.drawable.a7));
        }


        viewHolder.tvName.setText(arrFile.get(position).getName()+"");
        viewHolder.tvDate.setText("17/02/2017");
        viewHolder.imgOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int id= view.getId();
                switch (id){
                    case R.id.imgOption:
                        PopupMenu popup = new PopupMenu(getContext(), view);
                        popup.getMenuInflater().inflate(R.menu.popup_menu,
                                popup.getMenu());
                        popup.show();
                        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                            @Override
                            public boolean onMenuItemClick(MenuItem item) {

                                switch (item.getItemId()) {
                                    case R.id.openFile:
                                        MimeTypeMap myMime = MimeTypeMap.getSingleton();
                                        Intent newIntent = new Intent(Intent.ACTION_VIEW);
                                        String mimeType = myMime.getMimeTypeFromExtension(fileExt(arrFile.get(position).getName()).substring(1));
                                        Log.e("mimeType",mimeType);
                                        File file=new File(arrFile.get(position).getPathFileInStorage());
                                        newIntent.setDataAndType(Uri.fromFile(file),mimeType);
                                        newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                        try {
                                            context.startActivity(newIntent);
                                        } catch (ActivityNotFoundException e) {
                                            Toast.makeText(context, "No handler for this type of file.", Toast.LENGTH_LONG).show();
                                        }
                                        break;
                                    case R.id.showDetail:
                                        Toast.makeText(getContext(), "Show detail at position " + " : " + position, Toast.LENGTH_LONG).show();
                                        break;
                                    case R.id.delete:
                                        Toast.makeText(getContext(), "Deleted at position " + " : " + position, Toast.LENGTH_LONG).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });

                        break;
                    default:
                        break;
                }
            }
        });

        return convertView;
    }

    class ViewHolder{
        private ImageView img, imgOption;
        private TextView tvName,tvDate;
    }

    private String fileExt(String url) {
        if (url.indexOf("?") > -1) {
            url = url.substring(0, url.indexOf("?"));
        }
        if (url.lastIndexOf(".") == -1) {
            return null;
        } else {
            String ext = url.substring(url.lastIndexOf(".") + 1);
            if (ext.indexOf("%") > -1) {
                ext = ext.substring(0, ext.indexOf("%"));
            }
            if (ext.indexOf("/") > -1) {
                ext = ext.substring(0, ext.indexOf("/"));
            }
            return ext.toLowerCase();

        }
    }

    public void filter(String charText) {
        charText = charText.toLowerCase(Locale.getDefault());
        arrFile.clear();
        if (charText.length() == 0) {
            arrFile.addAll(searchList);
        } else {
            for (FileHistory s : searchList) {
                String nameSearch= s.getName();
                if (nameSearch.toLowerCase(Locale.getDefault()).contains(charText)) {
                    arrFile.add(s);
                }
            }
        }
        notifyDataSetChanged();
    }
}
