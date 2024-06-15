package com.example.sesion9.service;

import com.example.sesion9.entity.Categoria;
import com.example.sesion9.entity.Pais;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface ServiceCategoriaLibro {
    @GET("servicio/util/listaCategoriaDeLibro")
    public Call<List<Categoria>> listarCategoriasLibro();
}
