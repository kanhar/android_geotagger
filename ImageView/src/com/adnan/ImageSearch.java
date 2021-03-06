package com.adnan;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.sanselan.ImageReadException;
import org.apache.sanselan.Sanselan;
import org.apache.sanselan.common.IImageMetadata;
import org.apache.sanselan.formats.jpeg.JpegImageMetadata;
import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;
import org.apache.sanselan.formats.tiff.constants.TiffConstants;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class ImageSearch extends Activity implements OnClickListener {
	String TAG = "IMGSEARCH";

	Button btnOk;
	EditText txtSearch;
	Gallery gallery;
	
	ImageAdapter mImageAdapter;
	AlertDialog.Builder mDialogbuilder;

	/** Called when the activity is first created. */
	//@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.image_search);
		btnOk = (Button) findViewById(R.id.btn_ok);
		btnOk.setOnClickListener(this);
		txtSearch = (EditText) findViewById(R.id.txt_search);
		gallery = (Gallery) findViewById(R.id.gallery);
		gallery.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView parent, View v,
					int position, long id) {
				String s = "";
				s += "Extra: ";
				s += (mImageAdapter.getDesc(position) + "\n");
				if (mImageAdapter.hasLoc(position)) {
					s += "Latitude: ";
					s += (mImageAdapter.getLat(position) + "\n");
					s += "Longitude: ";
					s += (mImageAdapter.getLong(position) + "\n");
				}
				Toast.makeText(ImageSearch.this, s, Toast.LENGTH_SHORT)
						.show();
			}
		});
		mDialogbuilder = new AlertDialog.Builder(this);
		mDialogbuilder.setTitle("Error");
		mDialogbuilder.setIcon(android.R.drawable.ic_dialog_info);
		mDialogbuilder.setPositiveButton("OK", null);
	}

	@Override
	public void onClick(View v) {
		String strSearch = txtSearch.getText().toString().trim();
		ArrayList<String> imgList = new ArrayList<String>();
		if (strSearch.equals("")) {
			mDialogbuilder.setMessage("Please enter search criteria");
			return;
		}
		
		File dir = new File("/sdcard/DCIM/Camera");

		for (File file : dir.listFiles()) {
			if (file.isDirectory())
				continue;
					
			IImageMetadata metadata = null;
			JpegImageMetadata jpegMetadata = null;
			TiffImageMetadata exif = null;
			
			try {
				metadata = Sanselan.getMetadata(file);
			} catch (ImageReadException ire) {
				Log.d(TAG, "ImageReadException file=" + file.getName());
			} catch (IOException ioe) {
				Log.d(TAG, "IOException file=" + file.getName());
			}
			// establish jpegMedatadata
			if (metadata != null) {
				jpegMetadata = (JpegImageMetadata) metadata;
			}

			// establish exif
			if (jpegMetadata != null) {
				exif = jpegMetadata.getExif();
			}

			if (exif != null) {
				try {
					TiffField desc = exif
							.findField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);

					if(desc!=null && desc.getStringValue().contains(strSearch)){
						imgList.add(file.getAbsolutePath());
					}
					
				} catch (ImageReadException e) {
					e.printStackTrace();
				}
			}

		}
		if(imgList.size()==0){
			mDialogbuilder.setMessage("No images with '"+strSearch+"' in extra info can be found.");
			mDialogbuilder.show();
			return;
		}
		//mDialogbuilder.setMessage(msg);
		//mDialogbuilder.show();
		Log.d(TAG, "starting viewer list size="+imgList.size());
		mImageAdapter = new ImageAdapter(this, imgList.toArray());
		gallery.setAdapter(mImageAdapter);
	}

	class ImageAdapter extends BaseAdapter {
		int mGalleryItemBackground;
		private Context mContext;
		private Object[] mFileNames;
		private String[] mDesc;
		private boolean[] mHasLoc;
		private double[] mLatitude;
		private double[] mLongitude;
		private int nothing;
		
		public ImageAdapter(Context c, Object[] fileNames) {
			mContext = c;
			mFileNames = fileNames;
			mDesc = new String[mFileNames.length];
			mHasLoc = new boolean[mFileNames.length];
			mLatitude = new double[mFileNames.length];
			mLongitude = new double[mFileNames.length];

			for (int i = 0; i < fileNames.length; i++) {
				File file = new File(fileNames[i].toString());

				IImageMetadata metadata = null;
				JpegImageMetadata jpegMetadata = null;
				TiffImageMetadata exif = null;

				try {
					metadata = Sanselan.getMetadata(file);
				} catch (ImageReadException ire) {
					Log.d(TAG, "ImageReadException file=" + file.getName());
				} catch (IOException ioe) {
					Log.d(TAG, "IOException file=" + file.getName());
				}
				// establish jpegMedatadata
				if (metadata != null) {
					jpegMetadata = (JpegImageMetadata) metadata;
				}

				// establish exif
				if (jpegMetadata != null) {
					exif = jpegMetadata.getExif();
				}

				if (exif != null) {
					try {
						TiffImageMetadata.GPSInfo gpsInfo = exif.getGPS();
						TiffField desc = exif
								.findField(TiffConstants.EXIF_TAG_IMAGE_DESCRIPTION);

						mDesc[i] = desc.getStringValue();
						if (gpsInfo != null) {
							mHasLoc[i] = true;
							mLongitude[i] = gpsInfo.getLongitudeAsDegreesEast();
							mLatitude[i] = gpsInfo.getLatitudeAsDegreesNorth();

						}

					} catch (ImageReadException e) {
						e.printStackTrace();
					}
				}

			}

			TypedArray a = obtainStyledAttributes(R.styleable.Theme);
			mGalleryItemBackground = a.getResourceId(
					R.styleable.Theme_android_galleryItemBackground, 0);
			a.recycle();
		}

		public String getDesc(int position) {
			return mDesc[position];
		}

		public boolean hasLoc(int position) {
			return mHasLoc[position];
		}

		public double getLat(int position) {
			return mLatitude[position];
		}

		public double getLong(int position) {
			return mLongitude[position];
		}

		public int getCount() {
			return mFileNames.length;
		}

		public Object getItem(int position) {
			return position;
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			try {
				ImageView i = new ImageView(mContext);
				Bitmap bm = BitmapFactory.decodeFile(mFileNames[position]
						.toString());
				if (bm != null)
					Log.d(TAG, "successful decdode file="
							+ mFileNames[position]);
				else
					Log.d(TAG, "un-successful decdode file="
							+ mFileNames[position]);
				i.setImageBitmap(bm);
				i.setLayoutParams(new Gallery.LayoutParams(150, 170));
				i.setScaleType(ImageView.ScaleType.FIT_CENTER);
				Log.d(TAG, "returning ImageView");
				return i;
			} catch (Exception e) {
				Log.d(TAG, "error in getView()");
				if (e != null && e.getMessage() != null)
					Log.d(TAG, e.getMessage());
				return null;
			}
		}

		public float getScale(boolean focused, int offset) {
			Log.d(TAG, "getScale()");
			/* Formula: 1 / (2 ^ offset) */
			return Math.max(0, 1.0f / (float) Math.pow(2, Math.abs(offset)));
		}
	}
}