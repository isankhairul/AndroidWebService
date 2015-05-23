package com.example.android_webservice;

public class Barang {

	private int barangId;
	private String nama;
	private int harga;
	
	public Barang(int barangId, String nama, int harga) {
		super();
		this.barangId = barangId;
		this.nama = nama;
		this.harga = harga;
	}
	
	public int getBarangId() {
		return barangId;
	}
	public void setBarangId(int barangId) {
		this.barangId = barangId;
	}
	public String getNama() {
		return nama;
	}
	public void setNama(String nama) {
		this.nama = nama;
	}
	public int getHarga() {
		return harga;
	}
	public void setHarga(int harga) {
		this.harga = harga;
	}
	
}
