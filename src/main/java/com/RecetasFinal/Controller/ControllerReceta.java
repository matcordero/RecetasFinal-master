package com.RecetasFinal.Controller;

import java.io.IOException;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.multipart.MultipartFile;


import com.RecetasFinal.Entities.Calificacion;
import com.RecetasFinal.Entities.Foto;
import com.RecetasFinal.Entities.Ingrediente;
import com.RecetasFinal.Entities.Paso;
import com.RecetasFinal.Entities.Receta;
import com.RecetasFinal.Entities.Sesion;
import com.RecetasFinal.Entities.Tipo;
import com.RecetasFinal.Entities.Usuario;
import com.RecetasFinal.Entities.Utilizado;
import com.RecetasFinal.Services.*;

@CrossOrigin
@RestController
@RequestMapping("/Recetas/Controller")

public class ControllerReceta {
	
	@Autowired
    private RecetaService recetaService;

    @Autowired
    private CalificacionService calificacionService;

    @Autowired
    private TipoService tipoService;
    
    @Autowired
    private IngredienteService ingredienteService;
    
    @Autowired
    private UsuarioService usuarioService;
    
    @Autowired
    private SesionService sesionService;
    
    @Autowired
    private FotoService fotoService;
    
    @Autowired
	private CloudinaryService cloudinaryService;
    
    @Autowired
    private ServicioMail servicioMail;
    
    /*--Mandar Mail--*/
    
    @CrossOrigin
    @GetMapping(value = "/mail/{email}")
    public ResponseEntity<?> mail(@PathVariable String email){
    	try {
    		//this.servicioMail.sendListEmail(email);
    		return ResponseEntity.ok().body("Codigo Enviado");
		} catch (Exception e) {
			return ResponseEntity.unprocessableEntity().body("Fallo al mandar mail");
		}
    	
    }
    
    /*---Usuario---*/
	
    @CrossOrigin
    @GetMapping(value = "/login/{email}/{contrasena}")
    public ResponseEntity<?> getLogin(@PathVariable String email,@PathVariable String contrasena){
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		Sesion sesion = oSesion.get();
    		if(sesion.getPassword().equals(contrasena)) {
    			Usuario usuario = sesion.getUsuario();
    			if(usuario.getHabilitado()=="Si") {
    				return ResponseEntity.ok().body(sesion.getUsuario());
    			}
    			else {
    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Habilitado");
    			}
    		}
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
    @PostMapping(value = "/signUp1")
    public ResponseEntity<?> postSingUp1(@RequestParam("email") String email,@RequestParam("alias") String alias){
    	List<Usuario> usuario = usuarioService.findall();
    	List<String> mails = usuario.stream().map(u ->u.getMail()).toList();
    	List<String> nicknames = usuario.stream().map(u ->u.getNickname()).toList();
    	boolean contienemail= mails.contains(email);
    	boolean contienenick= nicknames.contains(alias);
    	if(contienemail == false && contienenick == false) {
    		Usuario usuarion = new Usuario();
    		usuarion.setMail(email);
    		usuarion.setNickname(alias);
    		usuarioService.save(usuarion);
    		return ResponseEntity.status(HttpStatus.CREATED).body(usuarion.getIdUsuario());
    	}
    	else if(contienemail) {
    		return ResponseEntity.unprocessableEntity().body("EMail ya existe");
    	}
    	else {
    		return ResponseEntity.unprocessableEntity().body(generarSimilares(alias, nicknames));
    	}
    }
    
    private List<String> generarSimilares(String alias, List<String> listaUsados) {
        List<String> cadenasSimilares = new ArrayList<>();
        while (cadenasSimilares.size()<3) {
        	Random random = new Random();
            int numero = random.nextInt(1000) + 1;
            String nuevoNombre = alias + numero;
            if(!listaUsados.contains(nuevoNombre) && !cadenasSimilares.contains(nuevoNombre)) {
            	cadenasSimilares.add(nuevoNombre);
            }
        }
        
        return cadenasSimilares;
    }
    
    @PutMapping(value = "/signUp2")
    public ResponseEntity<?> putSingUp2(@RequestParam("idUsuario") int idUsuario,@RequestParam("nombre") String nombre,
            @RequestParam("contrasena") String contrasena){
        Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        if(oUsuario.isPresent()) {
            Usuario usuario = oUsuario.get();
            usuario.setNombre(nombre);
            usuario.setSesion(new Sesion(usuario.getMail(), contrasena, usuario));
            usuario.setTipoUsuario("Visitante");
            usuarioService.save(usuario);
            return ResponseEntity.ok().body("Cuenta creada");
        }
        else {
            return ResponseEntity.unprocessableEntity().body("Error en los datos");
        }
    }
	
    
    @PutMapping(value = "/cambiarFoto")
    public ResponseEntity<?> cambiarFoto(@RequestParam("email") String email,@RequestParam("contrasena") String contrasena,@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException{
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		Sesion sesion = oSesion.get();
    		if(sesion.getPassword().equals(contrasena)) {
    			Map<?, ?> result = cloudinaryService.upload(multipartFile);
    			sesion.getUsuario().setAvatar(result.get("url").toString());
    			usuarioService.save(sesion.getUsuario());
    			return ResponseEntity.status(HttpStatus.OK).body("Avatar Cambiado");
    		}
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
	
	
	@GetMapping(value = "/recetasIntentar/{id}")
    public ResponseEntity<?> getLogin(@PathVariable Integer id){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(id);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		return ResponseEntity.status(HttpStatus.OK).body(usuario.getRecetasIntentar());
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
	
	@GetMapping(value = "/recetasCredas/{id}")
    public ResponseEntity<?> getCreadas(@PathVariable Integer id){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(id);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		return ResponseEntity.status(HttpStatus.OK).body(usuario.getRecetasCreadas());
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
	
    
    @PutMapping(value = "/cambiarContrasena")
    public ResponseEntity<?> putCambiarContrasena(@RequestParam("idUsuario") int idUsuario,@RequestParam("contrasenaNueva") String contrasenaNueva){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		Sesion sesion = usuario.getSesion();
    		sesion.setPassword(contrasenaNueva);
    		sesionService.modificarSesion(sesion);
    		return ResponseEntity.ok().body("Contraseña Cambiada");
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
    
    @PutMapping(value = "/mandarCodigo")
    public ResponseEntity<?> putCambiarContrasena(@RequestParam("email") String email){
    	String randomNumber = String.format("%06d", new Random().nextInt(1000000));
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		try {
    			String mensaje = "Codigo de recuperacion de cuenta es: " + randomNumber;
        		this.servicioMail.sendListEmail(email,mensaje);
        		Sesion sesion = oSesion.get();
        		sesion.setCodigo(randomNumber);
        		sesionService.modificarSesion(sesion);
        		return ResponseEntity.ok().body("Codigo Enviado");
    		} catch (Exception e) {
    			return ResponseEntity.unprocessableEntity().body("Fallo al mandar mail");
    		}
    	}
    	else {
    		return ResponseEntity.unprocessableEntity().body("Mail no Registrado");
    	}
    }
    
    @PutMapping(value = "/recuperarCuenta")
    public ResponseEntity<?> putCambiarContrasena(@RequestParam("email") String email,
    		@RequestParam("contrasena") String contrasea,@RequestParam("codigo") String codigo){
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		Sesion sesion = oSesion.get();
    		if(codigo.equals(sesion.getCodigo())) {
    			sesion.setCodigo(null);
    			sesion.setPassword(contrasea);
    			sesionService.modificarSesion(sesion);
    			return ResponseEntity.ok().body("Contraseña Cambiada");
    		}
        	return ResponseEntity.unprocessableEntity().body("Codigo Incorrecto o Ya usado");
    	}
    	else {
    		return ResponseEntity.unprocessableEntity().body("Mail no Registrado");
    	}
    }
    
    @GetMapping(value = "/getAllUsuarios")
    public ResponseEntity<?> getAllUsuarios() {
    	return ResponseEntity.ok().body(usuarioService.findall());
    }
    /*---Obtener Recetas---*/
   
    @CrossOrigin
    @GetMapping(value = "/ultimasTresRecetas")
    public ResponseEntity<?> ultimasTresRecetas() {

        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        List<Receta> ultimasTresRecetas = new ArrayList<>();
        int totalRecetas = todasRecetas.size();
        if (totalRecetas >= 3) {
            ultimasTresRecetas = todasRecetas.subList(totalRecetas - 3, totalRecetas);
        } else {
            ultimasTresRecetas = todasRecetas;
        }
        return ResponseEntity.ok().body(ultimasTresRecetas);
    }
    
    @GetMapping(value = "/getRecipe/{id}")
    public ResponseEntity<?> getRecetasByNombre(@PathVariable Integer id) {
        Optional<Receta> oReceta = recetaService.findById(id);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	return ResponseEntity.ok().body(receta);
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @GetMapping(value = "/modificarCantidadSimple/{id}/{operacion}")
    public ResponseEntity<?> modificarCantidadSimple(@PathVariable Integer id,@PathVariable Integer operacion) {
        Optional<Receta> oReceta = recetaService.findById(id);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	if(operacion == 0) {
        		receta.getRecetaMitad();
        	}
        	else {
        		receta.getRecetaDoble();
        	}
        	return ResponseEntity.ok().body(receta);
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @GetMapping(value = "/modificarCantidadAvanzado/{idReceta}/{idIngrediente}/{cantidad}")
    public ResponseEntity<?> modificarCantidadAvanzado(@PathVariable Integer idReceta,@PathVariable Integer idIngrediente,@PathVariable Integer cantidad) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Ingrediente> oIngrediente = ingredienteService.findById(idIngrediente);
        	if(oIngrediente.isPresent()) {
        		Ingrediente ingrediente = oIngrediente.get();
        		receta.modificarIngrediente(ingrediente, cantidad);
        		ResponseEntity.ok().body(receta);
        	}
        	ResponseEntity.unprocessableEntity().body("Ingrediente no Encontrada");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @PostMapping(value = "/insertarComentarios")
    public ResponseEntity<?> insertarComentarios(@RequestParam("idUsuario") int idUsuario,
    		@RequestParam("contrasena") String contrasena, @RequestParam("idReceta") int idReceta,
    		@RequestParam("comentario") String comentario,@RequestParam("idUsuario") int valoracion) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        	if(oUsuario.isPresent()) {
        		Usuario usuario = oUsuario.get();
        		if(usuario.getSesion().getPassword().equals(contrasena)) {
        			Calificacion calificacion = new Calificacion();
        			calificacion.setCalificacion(valoracion);
        			calificacion.setComentarios(comentario);
        			calificacion.setReceta(receta);
        			calificacion.setUsuario(usuario);
        			calificacionService.save(calificacion);
        			receta.addCalificacion(calificacion);
        			recetaService.save(receta);
        			return ResponseEntity.ok().body(receta.getCalificaciones());
        		}
        		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
        	}
        	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @GetMapping(value = "/getMisRecetas/{idUsuario}/{contrasena}")
    public ResponseEntity<?> getMisRecetas(@PathVariable Integer idUsuario,@PathVariable String contrasena) {
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		if(usuario.getSesion().getPassword().equals(contrasena)) {
    			return ResponseEntity.ok().body(usuario.getRecetasIntentar());
    		}
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Valido");
    }
    
    @GetMapping(value = "/validarNombre/{nombreReceta}/{idUsuario}")
    public ResponseEntity<?> validarNombre(@PathVariable String nombreReceta,@PathVariable int idUsuario) {
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		List<Receta> recetas = usuario.getRecetasCreadas();
    		boolean nombreUsado = 0<recetas.stream().filter(r -> r.getNombre().equals(nombreReceta)).count();
    		if(nombreUsado) {
    			return ResponseEntity.unprocessableEntity().body("Nombre Usado");
    		}
    		return ResponseEntity.ok().body("Nombre Disponible");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Valido");
    }
    
    @GetMapping(value = "/getIngredientes")
    public ResponseEntity<?> getIngredientes() {
    	return ResponseEntity.ok().body(ingredienteService.findAll());
    }
    
    @PostMapping(value = "/CargarReceta")
    public ResponseEntity<?> insertarComentarios(@RequestParam("idUsuario") int idUsuario,
    		@RequestParam("contrasena") String contrasena, @RequestParam("nombre") String nombre, 
    		//@RequestParam("utilizados") List<Utilizado> utilizados, 
    		//@RequestParam("pasos") List<Paso> pasos, 
    		@RequestParam("descripcion") String descripcion,  		 
    		@RequestParam("porciones") int porciones,
    		@RequestParam("cantPersonas") Integer cantPersonas,
    		@RequestParam("foto") MultipartFile foto
    		//,@RequestParam("fotos") List<MultipartFile> fotos
    		) throws IOException{
    	
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		if(usuario.getSesion().getPassword().equals(contrasena)) {
    			Receta receta = new Receta();
    			receta.setUsuario(usuario);
    			receta.setCantidadPersonas(cantPersonas);
    			receta.setDescripcion(descripcion);
    			receta.setNombre(nombre);
    			receta.setPorciones(porciones);
    			//-------------------------
    			Tipo tipo = tipoService.getTipobyId(1).get();
    			receta.setTipo(tipo);
    			
    			System.err.println("aca0");
    			recetaService.save(receta);
    			System.err.println(receta.getIdReceta());
    			//-----------------------------
    			System.err.println("aca1");
    			Map resultFoto = cloudinaryService.upload(foto);
    			String originalFilename = foto.getOriginalFilename();
    	        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);
    			Foto fotoCargada = new Foto(receta,resultFoto.get("url").toString(),extension);
    			fotoService.save(fotoCargada);
    			receta.setFoto(fotoCargada);
    			System.err.println("aca2");
    			//-------------------------------
    			/*List<Foto> fotosUrl = new ArrayList<Foto>();
    			for(MultipartFile f:fotos) {
    				Map fotoUrl = cloudinaryService.upload(f);
    				fotosUrl.add(new Foto(receta,fotoUrl.get("url").toString(),"imagen"));
    			}
    			
    			receta.setFotos(fotosUrl);*/
    			
    			return ResponseEntity.ok().body(recetaService.save(receta));
    			
    			
    			
    			
    		}
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Valido");
    }
    
    
    
    
    
    @GetMapping(value = "/buscarReceta/{nombre}")
    public ResponseEntity<?> getRecetasByNombre(@PathVariable String nombre) {
        List<Receta> todasRecetas = recetaService.obtenerRecetasbyNombre(nombre);
        todasRecetas.sort(Comparator.comparing(Receta::getIdReceta).reversed());
        return ResponseEntity.ok().body(todasRecetas);
    }
    
    @GetMapping(value = "/buscarCategoria/{idTipo}")
    public ResponseEntity<?> getRecetasByTipo(@PathVariable Integer idTipo) {
        Optional<Tipo> oTipo = tipoService.getTipobyId(idTipo);
        if(oTipo.isPresent()) {
        	Tipo tipo = oTipo.get();
        	List<Receta> recetas = recetaService.obtenerRecetasPorTipo(tipo);
        	return ResponseEntity.ok().body(recetas);
        }
        return ResponseEntity.unprocessableEntity().body("Ese Tipo no existe");
    }
    
    @GetMapping(value = "/recetasByUsuario/{idUsuario}")
    public ResponseEntity<?> getRecetasByUsuario(@PathVariable Integer idUsuario) {
        Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        if(oUsuario.isPresent()) {
        	Usuario usuario = oUsuario.get();
        	List<Receta> recetas = recetaService.obtenerRecetasByUsuario(usuario);
        	return ResponseEntity.ok().body(recetas);
        }
        return ResponseEntity.unprocessableEntity().body("Ese Usuario no existe");
    }
    
    @GetMapping(value = "/recetasOrderByUsuario")
    public ResponseEntity<?> getRecetasOrderByUsuario() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        todasRecetas.sort(Comparator.comparing(Receta::getNombreUsuario));
        return ResponseEntity.ok().body(todasRecetas);
    }
    
    @GetMapping(value = "/recetasOrderByReceta")
    public ResponseEntity<?> getRecetasOrderByReceta() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        todasRecetas.sort(Comparator.comparing(Receta::getNombre));
        return ResponseEntity.ok().body(todasRecetas);
    }
    
    @GetMapping(value = "/recetasOrderByAntiguedad")
    public ResponseEntity<?> getRecetasOrderByAntiguedad() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        return ResponseEntity.ok().body(todasRecetas);
    }
    
    @GetMapping(value = "/getRecetasByIngrediente/{id}")
    public ResponseEntity<?> getRecetasByIngrediente(@PathVariable Integer idIngrediente) {
    	Optional<Ingrediente> oIngrediente = ingredienteService.findById(idIngrediente);
    	if(oIngrediente.isPresent()) {
    		Ingrediente ingrediente = oIngrediente.get();
    		List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
            return ResponseEntity.ok().body(todasRecetas.stream().filter(r -> r.tieneIngrediente(ingrediente)).collect(Collectors.toList()));
    	}
    	return ResponseEntity.unprocessableEntity().body("Ese Ingrediente no existe");
    }
    
    @GetMapping(value = "/getRecetasByNoIngrediente/{id}")
    public ResponseEntity<?> getRecetasByNoIngrediente(@PathVariable Integer idIngrediente) {
    	Optional<Ingrediente> oIngrediente = ingredienteService.findById(idIngrediente);
    	if(oIngrediente.isPresent()) {
    		Ingrediente ingrediente = oIngrediente.get();
    		List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
            return ResponseEntity.ok().body(todasRecetas.stream().filter(r -> r.tieneIngrediente(ingrediente)==false).collect(Collectors.toList()));
    	}
    	return ResponseEntity.unprocessableEntity().body("Ese Ingrediente no existe");
    }
    
}
