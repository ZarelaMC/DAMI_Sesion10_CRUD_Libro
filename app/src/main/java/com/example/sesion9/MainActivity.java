package com.example.sesion9;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.sesion9.adapter.LibroAdapter;
import com.example.sesion9.entity.Libro;
import com.example.sesion9.service.ServiceLibro;
import com.example.sesion9.util.ConnectionRest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class MainActivity  extends AppCompatActivity {

    EditText txtTitulo;
    Button btnConsultar,btnRegistrar;

    ListView lstConsultaLibro;
    ArrayList<Libro> data = new ArrayList<Libro>();
    LibroAdapter adaptador;

    ServiceLibro servicio;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        txtTitulo = findViewById(R.id.txtRegTitulo);

        lstConsultaLibro = findViewById(R.id.lstConsultaLibros);
        adaptador = new LibroAdapter(this, R.layout.activity_libro_item_crud, data);
        lstConsultaLibro.setAdapter(adaptador);

        servicio = ConnectionRest.getConnection().create(ServiceLibro.class);

        //Métodos de acción para los botones y el ListView
        btnConsultar = findViewById(R.id.btnLista);
        btnConsultar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String filtro = txtTitulo.getText().toString();
                consulta(filtro);
            }
        });


        btnRegistrar = findViewById(R.id.btnRegistrar);
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //mensajeAlert("Registrar");
                Intent intent = new Intent(MainActivity.this, LibroFormularioCrudActivity.class);
                intent.putExtra("var_metodo","REGISTRAR");
                startActivity(intent);
            }
        });

        lstConsultaLibro = findViewById(R.id.lstConsultaLibros);
        lstConsultaLibro.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //mensajeAlert("Actualiza >> " + position);
                Libro obj =data.get(position);

                Intent intent = new Intent(MainActivity.this, LibroFormularioCrudActivity.class);
                intent.putExtra("var_metodo","ACTUALIZAR");
                intent.putExtra("var_objeto",obj);

                startActivity(intent);
            }
        });
    }


    public  void consulta(String filtro){

        Call<List<Libro>> call = servicio.listaPorTitulo(filtro);
        call.enqueue(new Callback<List<Libro>>() {
            @Override
            public void onResponse(Call<List<Libro>> call, Response<List<Libro>> response) {

                if(response.isSuccessful()){
                    List<Libro> lstSalida = response.body();
                    data.clear();
                    data.addAll(lstSalida);
                    adaptador.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<Libro>> call, Throwable t) {

                mensajeAlert("ERROR -> Error en la respuesta" + t.getMessage());
            }
        });


    }

    public  void mensajeAlert(String msg){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}