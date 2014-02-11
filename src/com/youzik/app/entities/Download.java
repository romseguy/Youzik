package com.youzik.app.entities;

import android.os.Parcel;
import android.os.Parcelable;

public class Download implements Parcelable {
	
	private long id;
	private String name = "";
	private String url = "";

	public static final Parcelable.Creator<Download> CREATOR = new Parcelable.Creator<Download>() {
		@Override
		public Download createFromParcel(Parcel source) {
			Download dl = new Download();
			dl.setId(source.readLong());
			dl.setName(source.readString());
			dl.setUrl(source.readString());
			return dl;
		}

		@Override
		public Download[] newArray(int size) {
			return new Download[size];
		}
	};
	
	@Override
	public void writeToParcel(Parcel dst, int flag) {
		dst.writeLong(this.id);
		dst.writeString(this.name);
		dst.writeString(this.url);
	}
	
	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = "".equals(name)? "Unknown download title" : name;
	}

	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public String toString() {
		return "Download [id=" + this.id + ", name=" + this.name + ", url=" + this.url + "]";
	}

}
