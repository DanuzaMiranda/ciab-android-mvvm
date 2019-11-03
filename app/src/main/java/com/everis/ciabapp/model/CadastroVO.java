package com.everis.ciabapp.model;

import android.os.Parcel;
import android.os.Parcelable;

import com.squareup.moshi.Json;

public class CadastroVO implements Parcelable {

    public static final Creator<CadastroVO> CREATOR = new Creator<CadastroVO>() {
        @Override
        public CadastroVO createFromParcel(Parcel in) {
            return new CadastroVO(in);
        }

        @Override
        public CadastroVO[] newArray(int size) {
            return new CadastroVO[size];
        }
    };

    @Json(name = "id")
    private int id;
    @Json(name = "name")
    private String nome;
    @Json(name = "email")
    private String email;
    @Json(name = "company")
    private String empresa;
    @Json(name = "balance")
    private int saldo;
    @Json(name = "order")
    private String order;
    @Json(name = "embedding")
    private String embedding;

    public CadastroVO() {

    }

    private CadastroVO(Parcel in) {
        id = in.readInt();
        nome = in.readString();
        empresa = in.readString();
        email = in.readString();
        saldo = in.readInt();
        order = in.readString();
        embedding = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(id);
        dest.writeString(nome);
        dest.writeString(empresa);
        dest.writeString(email);
        dest.writeInt(saldo);
        dest.writeString(order);
        dest.writeString(embedding);
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

    public String getEmpresa() {
        return empresa;
    }

    public void setEmpresa(String empresa) {
        this.empresa = empresa;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getEmbedding() {
        return embedding;
    }

    public void setEmbedding(String embedding) {
        this.embedding = embedding;
    }
}
