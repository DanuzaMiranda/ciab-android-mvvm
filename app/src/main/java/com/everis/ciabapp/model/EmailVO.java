package com.everis.ciabapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public class EmailVO implements Parcelable {

    public static final Creator<EmailVO> CREATOR = new Creator<EmailVO>() {
        @Override
        public EmailVO createFromParcel(Parcel in) {
            return new EmailVO(in);
        }

        @Override
        public EmailVO[] newArray(int size) {
            return new EmailVO[size];
        }
    };
    @Json(name = "Users_id")
    private int id;
    @Json(name = "Users_name")
    private String nome;
    @Json(name = "Users_email")
    private String email;
    @Json(name = "Users_company")
    private String empresa;
    @Json(name = "Users_balance")
    private int saldo;
    @Json(name = "Users_order")
    private String order;

    public EmailVO() {

    }

    private EmailVO(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        email = in.readString();
        empresa = in.readString();
        saldo = in.readInt();
        order = in.readString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public int getSaldo() {
        return saldo;
    }

    public void setSaldo(int saldo) {
        this.saldo = saldo;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nome);
        dest.writeString(email);
        dest.writeString(empresa);
        dest.writeInt(saldo);
        dest.writeString(order);
    }
}
