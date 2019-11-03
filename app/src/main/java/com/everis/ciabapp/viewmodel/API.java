package com.everis.ciabapp.viewmodel;

import com.everis.ciabapp.model.CadastroVO;
import com.everis.ciabapp.model.EmailVO;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface API {

    String BASE_URL = "http://35.247.228.196:8080";

    //Cadastro de usuário
    @POST("/users")
    Call<CadastroVO> saveUser(@Body CadastroVO cadastroVO);

    //Cadastro de foto
    @POST("/users/{id}/image")
    @Multipart
    Call<ResponseBody> saveImage(@Path("id") int id,
                                 @Part MultipartBody.Part file);

    //Puxar usuário por e-mail
    @GET("/emails/{email}")
    Call<EmailVO> getEmailUser(@Path("email") String email);

    //Puxar informação do usuário
    @GET("/users/{id}")
    Call<CadastroVO> getUser(@Path("id") int id);

    //Reconhecimento Facial
    @POST("/images")
    @Multipart
    Call<CadastroVO> imageRecog(@Part MultipartBody.Part file);

    //Tranferência monetária
    @PUT("/users/{creditorId}/transfer/{debtorId}")
    Call<ResponseBody> makeTransfer(@Path("creditorId") int cId,
                                    @Path("debtorId") int dId);

    //Atualizar dados do usuário
    @PUT("/users/{id}")
    Call<CadastroVO> endJourney(@Path("id") int id,
                                @Body CadastroVO cadastroVO);

}
