package com.example.sesion9.service;

import com.example.sesion9.entity.Pais;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServicePais {
    @GET("servicio/util/listaPais")
    public Call<List<Pais>> listarPaises();
}
