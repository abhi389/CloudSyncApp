package com.se1.DropBox;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dropbox.client2.DropboxAPI.Entry;
import com.se1.main.R;

import java.util.ArrayList;

public class DownloadFileAdapter extends BaseAdapter {

	// private Context context;
	private ArrayList<Entry> files;
	private View view;
	private LayoutInflater lInflater;

	public DownloadFileAdapter(Context context, ArrayList<Entry> files) {
		// this.context = context;
		this.files = files;
		lInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		return files.size();
	}

	@Override
	public Object getItem(int position) {
		return position;
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private class Holder {
		ImageView ivImageFolderOrFile, ivImageDownloadOrBrowableDir;
		TextView tvDownloadFileOrFolderName;

	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		final Holder holder;
		view = convertView;
		if (view == null) {
			holder = new Holder();
			view = lInflater.inflate(R.layout.downloadfileinflater, null);


			//holder.ivImageDownloadOrBrowableDir = (ImageView) view
			//		.findViewById(R.id.ivImageDownloadOrBrowableDir);
			holder.ivImageFolderOrFile = (ImageView) view
					.findViewById(R.id.ivImageFolderOrFile);
			holder.tvDownloadFileOrFolderName = (TextView) view
					.findViewById(R.id.tvDownloadFileFileName);
			view.setTag(holder);
		} else {
			holder = (Holder) view.getTag();
		}
		Entry file = files.get(position);
        char[] fileNameCharArray=null;

        if(file.mimeType!=null && (getFileType(file.mimeType.toCharArray()).equalsIgnoreCase("image")))
        {
            holder.ivImageFolderOrFile
                    .setImageResource(R.drawable.image_icon);
        }
        else if(file.mimeType!=null && (getFileType(file.mimeType.toCharArray()).equalsIgnoreCase("video")))
        {
            holder.ivImageFolderOrFile
                    .setImageResource(R.drawable.video_icon);
        }
        else if(file.mimeType!=null && (getFileType(file.mimeType.toCharArray()).equalsIgnoreCase("audio")))
        {
            holder.ivImageFolderOrFile
                    .setImageResource(R.drawable.audio_icon);
        }
        else if (!file.isDir) {
			//holder.ivImageDownloadOrBrowableDir
			//		.setImageResource(R.drawable.downloadicon);
			holder.ivImageFolderOrFile.setImageResource(R.drawable.file_icon);
		}
        else {
			//holder.ivImageDownloadOrBrowableDir
			//		.setImageResource(R.drawable.browsedirectoryicon);
			holder.ivImageFolderOrFile
					.setImageResource(R.drawable.folder_icon);
		}
        holder.tvDownloadFileOrFolderName.setTextColor(-16777216);
		holder.tvDownloadFileOrFolderName.setText(file.fileName());
		return view;
	}
    public static String getFileType(char[] fileNameChar)
    {
        String docType="";
        for(int charArrayIndex=0;charArrayIndex<fileNameChar.length;charArrayIndex++)
        {
            if(fileNameChar[charArrayIndex]=='/')
                break;
            docType=docType+fileNameChar[charArrayIndex];
        }
        return docType;
    }

}
