package com.example.sesion9;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.sesion9.entity.Categoria;
import com.example.sesion9.entity.Libro;
import com.example.sesion9.entity.Pais;
import com.example.sesion9.service.ServiceCategoriaLibro;
import com.example.sesion9.service.ServiceLibro;
import com.example.sesion9.service.ServicePais;
import com.example.sesion9.util.ConnectionRest;
import com.example.sesion9.util.FunctionUtil;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LibroFormularioCrudActivity extends AppCompatActivity {

    Button btnRegistrar, btnRegresar, btnEnviar;

    String metodo; //para recoger la var_metodo enviada desde el MainActivity

    //Pais
    Spinner spnPais;
    ArrayAdapter<String> adaptadorPais;
    ArrayList<String> paises = new ArrayList<>();

    //Categoria
    Spinner spnCategoria;
    ArrayAdapter<String> adaptadorCategoria;
    ArrayList<String> categorias = new ArrayList<>();

    //Servicio
    ServiceLibro serviceLibro;
    ServicePais servicePais;
    ServiceCategoriaLibro serviceCategoriaLibro;

    EditText txtTitulo, txtAnio, txtSerie;

    TextView idTitlePage, txtEstado;

    Libro objActual;

    RadioButton rbtnActivo, rbtnInactivo;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_libro_formulario_crud);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        //INICIALIZAR SERVICE para el acceso a sus métodos
        serviceLibro = ConnectionRest.getConnection().create(ServiceLibro.class);
        servicePais = ConnectionRest.getConnection().create(ServicePais.class);
        serviceCategoriaLibro = ConnectionRest.getConnection().create(ServiceCategoriaLibro.class);

        //INSTANCIAR COMPONENETES (Adapter, Array y su Spinner relacionado)
        adaptadorPais = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, paises);
        spnPais = findViewById(R.id.spnRegLibPais);
        spnPais.setAdapter(adaptadorPais);

        adaptadorCategoria = new ArrayAdapter<>(this, androidx.appcompat.R.layout.support_simple_spinner_dropdown_item, categorias);
        spnCategoria = findViewById(R.id.spnRegLibCategoria);
        spnCategoria.setAdapter(adaptadorCategoria);


        txtTitulo = findViewById(R.id.txtRegLibTitulo);
        txtAnio = findViewById(R.id.txtRegLibAnio);
        txtSerie = findViewById(R.id.txtRegLibSerie);


        metodo = (String)getIntent().getExtras().get("var_metodo");

        idTitlePage = findViewById(R.id.idTitlePage);
        btnRegistrar = findViewById(R.id.btnRegistarLibro);
        btnRegresar = findViewById(R.id.btnRegresar);

        //Estado
        rbtnActivo = findViewById(R.id.rbtActivo);
        rbtnInactivo = findViewById(R.id.rbtInactivo);

        txtEstado = findViewById(R.id.txtEstado);

        if(metodo.equals("REGISTRAR"))
        {
            idTitlePage.setText("Registra Libro");
            btnRegistrar.setText("Registrar");

            //OCULTAR ESTADO
            txtEstado.setVisibility(View.INVISIBLE);
            rbtnActivo.setVisibility(View.INVISIBLE);
            rbtnInactivo.setVisibility(View.INVISIBLE);
        }
        else if(metodo.equals("ACTUALIZAR"))
        {
            idTitlePage.setText("Actualizar Libro");
            btnRegistrar.setText("Actualizar");

            objActual = (Libro)getIntent().getExtras().get("var_objeto");
            txtTitulo.setText(objActual.getTitulo());
            txtAnio.setText(String.valueOf(objActual.getAnio()));
            txtSerie.setText(objActual.getSerie());

            //Validar para controlar estado
            if (objActual.getEstado() == 1){
                rbtnActivo.setChecked(true);
            }else{
                rbtnInactivo.setChecked(true);
            }
        }

        btnRegresar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Si "metodo" = REGISTRAR o ACTUALIZAR
                Intent intent = new Intent(LibroFormularioCrudActivity.this, MainActivity.class); //VOLVER AL MISMO FORMULARIO
                startActivity(intent);

            }
        });

        cargaPaises();
        cargaCategoria();


        //Evento botón Registrar/Actualizar
        btnRegistrar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String titulo = txtTitulo.getText().toString();
                String anio = txtAnio.getText().toString();
                String serie= txtSerie.getText().toString();
                String idPais = spnPais.getSelectedItem().toString().split(":")[0];
                String idCategoria = spnCategoria.getSelectedItem().toString().split(":")[0];

                Pais objPais = new Pais();
                objPais.setIdPais(Integer.parseInt(idPais.trim()));

                Categoria objCategoria = new Categoria();
                objCategoria.setIdCategoria(Integer.parseInt(idCategoria.trim()));

                Libro objLibro = new Libro();
                objLibro.setTitulo(titulo);
                objLibro.setAnio(Integer.parseInt(anio));
                objLibro.setSerie(serie);
                objLibro.setPais(objPais);
                objLibro.setCategoria(objCategoria);
                objLibro.setFechaRegistro(FunctionUtil.getFechaActualStringDateTime());

                //Validar si es necesario Registrar Actualizar
                if (metodo.equals("REGISTRAR")){
                    objLibro.setEstado(1);
                    registra(objLibro);
                }else{
                    objLibro.setEstado(rbtnActivo.isChecked() ? 1 : 0);
                    objLibro.setIdLibro(objActual.getIdLibro());
                    actualiza(objLibro);
                }
            }
        });


    }


    void cargaPaises(){
        Call<List<Pais>> call = servicePais.listarPaises();
        call.enqueue(new Callback<List<Pais>>() {
            @Override
            public void onResponse(Call<List<Pais>> call, Response<List<Pais>> response) {
                if (response.isSuccessful()){
                    //Atrapar el cuerpo de la respuesta (datos)
                    List<Pais> lst = response.body();
                    paises.add("(Seleccione país)");
                    //para dar formato al dato mostrado
                    for(Pais obj: lst){
                        paises.add(obj.getIdPais() + " : " + obj.getNombre());
                    }
                    adaptadorPais.notifyDataSetChanged(); //refrescar el ArrayAdapter para que muestre los datos traídos

                    //Cargar el spinner con el Pais seleccionado
                    if(metodo.equals("ACTUALIZAR"))
                    {
                        String idSeleccionado  = String.valueOf(objActual.getPais().getIdPais());
                        String nombre = objActual.getPais().getNombre();
                        String row = idSeleccionado + " : " + nombre;
                        for(int i = 0; i <= lst.size(); i++){
                            if(paises.get(i).equals(row)){
                                spnPais.setSelection(i);
                                break;
                            }
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<List<Pais>> call, Throwable t) {

            }
        });
    }

    void cargaCategoria(){
        Call<List<Categoria>>  call = serviceCategoriaLibro.listarCategoriasLibro();
        call.enqueue(new Callback<List<Categoria>>() {
            @Override
            public void onResponse(Call<List<Categoria>> call, Response<List<Categoria>> response) {
                List<Categoria> lstAux =  response.body();
                categorias.add(" [ Seleccione ] ");
                for(Categoria aux: lstAux){
                    categorias.add(aux.getIdCategoria() + " : "  + aux.getDescripcion());
                }
                adaptadorCategoria.notifyDataSetChanged();

                //Cargar el spinner con la Categoria seleccionada
                if(metodo.equals("ACTUALIZAR"))
                {
                    String idSeleccionado  = String.valueOf(objActual.getCategoria().getIdCategoria());
                    String descrip = objActual.getCategoria().getDescripcion();
                    String row = idSeleccionado + " : " + descrip;
                    for(int i = 0; i <= lstAux.size(); i++){
                        if(categorias.get(i).equals(row)){
                            spnCategoria.setSelection(i);
                            break;
                        }
                    }
                }
            }
            @Override
            public void onFailure(Call<List<Categoria>> call, Throwable t) {

            }
        });
    }


    void registra(Libro obj){
        Call<Libro> call = serviceLibro.registraLibro(obj);
        call.enqueue(new Callback<Libro>() {
            @Override
            public void onResponse(Call<Libro> call, Response<Libro> response) {
                if (response.isSuccessful()){
                    Libro objSalida = response.body();
                    mensajeAlert(" Registro de Libro exitoso:  "
                            + " \n >>>> ID >> " + objSalida.getIdLibro()
                            + " \n >>> Título >>> " +  objSalida.getTitulo());
                }
            }
            @Override
            public void onFailure(Call<Libro> call, Throwable t) {

            }
        });
    }

    void actualiza(Libro obj){
        Call<Libro> call = serviceLibro.actualizaLibro(obj);
        call.enqueue(new Callback<Libro>() {
            @Override
            public void onResponse(Call<Libro> call, Response<Libro> response) {
                if (response.isSuccessful()){
                    Libro objSalida = response.body();
                    mensajeAlert(" Actualización de Libro exitosa:  "
                            + " \n >>>> ID >> " + objSalida.getIdLibro()
                            + " \n >>> Título >>> " +  objSalida.getTitulo());
                }
            }
            @Override
            public void onFailure(Call<Libro> call, Throwable t) {

            }
        });
    }


    void mensajeToast(String mensaje){
        Toast toast1 =  Toast.makeText(getApplicationContext(),mensaje, Toast.LENGTH_LONG);
        toast1.show();
    }
    public void mensajeAlert(String msg){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setMessage(msg);
        alertDialog.setCancelable(true);
        alertDialog.show();
    }
}

