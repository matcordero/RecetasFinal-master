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
    private UnidadService unidadService;
    
    @Autowired
    private ServicioMail servicioMail;
    
    /*--Cargar Fotos--*/
    @CrossOrigin
    @GetMapping(value = "/foto")
    public ResponseEntity<?> foto(@RequestParam("multipartFile") MultipartFile multipartFile) throws IOException{
    	try {
    		Map<?, ?> result = cloudinaryService.upload(multipartFile);
    		String originalFilename = multipartFile.getOriginalFilename();
    		String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
            Foto foto = new Foto();
            foto.setUrlFoto(result.get("url").toString());
            foto.setExtension(extension);
    		return ResponseEntity.ok().body(fotoService.save(foto));
		} catch (Exception e) {
			return ResponseEntity.unprocessableEntity().body("Fallo al cargar Foto");
		}
    	
    }
    
    
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
    @GetMapping(value = "/habilitarCuenta/{idUsuario}")
    public ResponseEntity<?> habilitarCuenta(@PathVariable Integer idUsuario){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		usuario.setHabilitado("Si");
    		usuarioService.save(usuario);
    		return ResponseEntity.ok().body("Cuenta Habilitada, regrese a la app para continuar");
    	}
    	return ResponseEntity.unprocessableEntity().body("No Habilitado");
    }
    
    
    @CrossOrigin
    @GetMapping(value = "/login/{email}/{contrasena}")
    public ResponseEntity<?> getLogin(@PathVariable String email,@PathVariable String contrasena){
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		Sesion sesion = oSesion.get();
    		if(sesion.getPassword().equals(contrasena)) {
    			Usuario usuario = sesion.getUsuario();
    			if(usuario.getHabilitado().equals("Si")){
    				return ResponseEntity.ok().body(sesion.getUsuario());
    			}
    			else {
    				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No Habilitado");
    			}
    		}
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
    
    @CrossOrigin
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
    		servicioMail.sendListEmail(email, "Seleccione el link para continuar con la creacion de la cuenta https://recetasfinal-master-production.up.railway.app/Recetas/Controller/habilitarCuenta/"+usuarion.getIdUsuario().toString());
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
    @CrossOrigin
    @PostMapping(value = "/signUp2")
    public ResponseEntity<?> putSingUp2(@RequestParam("idUsuario") int idUsuario,@RequestParam("nombre") String nombre,
            @RequestParam("contrasena") String contrasena){
        Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        if(oUsuario.isPresent()) {
            Usuario usuario = oUsuario.get();
            	usuario.setHabilitado("Si");
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
	
    @CrossOrigin
    @PostMapping(value = "/signUp2Foto")
    public ResponseEntity<?> putSingUp2Foto(@RequestParam("idUsuario") int idUsuario,@RequestParam("nombre") String nombre,
            @RequestParam("contrasena") String contrasena, @RequestParam("multipartFile") MultipartFile multipartFile) throws IOException{
        Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        if(oUsuario.isPresent()) {
            Usuario usuario = oUsuario.get();
            Map<?, ?> result = cloudinaryService.upload(multipartFile);
            usuario.setAvatar(result.get("url").toString());
            usuario.setHabilitado("Si");
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
    
    
    
    @CrossOrigin
    @PostMapping(value = "/cambiarFoto")
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
	
    @CrossOrigin
	@GetMapping(value = "/recetasIntentar/{id}")
    public ResponseEntity<?> recetasIntentar(@PathVariable Integer id){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(id);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		return ResponseEntity.status(HttpStatus.OK).body(usuario.getRecetasIntentar());
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
    @CrossOrigin
	@GetMapping(value = "/recetasCredas/{id}")
    public ResponseEntity<?> getRecetasCreadas(@PathVariable Integer id){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(id);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		return ResponseEntity.status(HttpStatus.OK).body(usuario.getRecetasCreadas());
    	}
    	return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales Incorrectas");
    }
	
    @CrossOrigin
    @PostMapping(value = "/cambiarContrasena")
    public ResponseEntity<?> postCambiarContrasena(@RequestParam("idUsuario") int idUsuario,@RequestParam("contrasenaNueva") String contrasenaNueva){
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
    @CrossOrigin
    @PostMapping(value = "/mandarCodigo")
    public ResponseEntity<?> postMandarCodigo(@RequestParam("email") String email){
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
    @CrossOrigin
    @PostMapping(value = "/recuperarCuenta")
    public ResponseEntity<?> postRecuperarCuenta(@RequestParam("email") String email,
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
    
    @CrossOrigin
    @PostMapping(value = "/validarCodigo")
    public ResponseEntity<?> postValidarCodigo(@RequestParam("email") String email,
    		@RequestParam("codigo") String codigo){
    	Optional<Sesion> oSesion = sesionService.findSesionByMail(email);
    	if(oSesion.isPresent()) {
    		Sesion sesion = oSesion.get();
    		System.out.println(codigo);
    		System.out.println(sesion.getCodigo());
    		if(codigo.equals(sesion.getCodigo())) {
    			return ResponseEntity.ok().body("Codigo Correcto");
    		}
        	return ResponseEntity.unprocessableEntity().body("Codigo Incorrecto o Ya usado");
    	}
    	else {
    		return ResponseEntity.unprocessableEntity().body("Mail no Registrado");
    	}
    }
    
    
    @CrossOrigin
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
    @CrossOrigin
    @GetMapping(value = "/getRecipe/{id}")
    public ResponseEntity<?> getRecetasByNombre(@PathVariable Integer id) {
        Optional<Receta> oReceta = recetaService.findById(id);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	return ResponseEntity.ok().body(receta);
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    @CrossOrigin
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
    @CrossOrigin
    @GetMapping(value = "/modificarCantidadAvanzado/{idReceta}/{idIngrediente}/{cantidad}")
    public ResponseEntity<?> modificarCantidadAvanzado(@PathVariable Integer idReceta,@PathVariable Integer idIngrediente,@PathVariable Integer cantidad) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Ingrediente> oIngrediente = ingredienteService.findById(idIngrediente);
        	if(oIngrediente.isPresent()) {
        		Ingrediente ingrediente = oIngrediente.get();
        		receta.modificarIngrediente(ingrediente, cantidad);
        		return ResponseEntity.ok().body(receta);
        	}
        	return ResponseEntity.unprocessableEntity().body("Ingrediente no Encontrada");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    
    
    
    @CrossOrigin
    @GetMapping(value = "/modificarCantidadPorciones/{idReceta}/{cantidad}")
    public ResponseEntity<?> modificarCantidadPorciones(@PathVariable Integer idReceta,@PathVariable Integer cantidad) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	receta.modificarPorciones(cantidad);
        	return ResponseEntity.ok().body(receta);
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    //TODO//
    @CrossOrigin
    @GetMapping(value = "/modificarCantidadPersonas/{idReceta}/{cantidad}")
    public ResponseEntity<?> modificarCantidadPersonas(@PathVariable Integer idReceta,@PathVariable Integer cantidad) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	receta.modificarCantPersonas(cantidad);
        	return ResponseEntity.ok().body(receta);
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @CrossOrigin
    @PostMapping(value = "/insertarCalificacion")
    public ResponseEntity<?> insertarComentarios(@RequestParam("idUsuario") int idUsuario,
    		@RequestParam("idReceta") int idReceta,@RequestParam("comentario") String comentario,@RequestParam("idUsuario") int valoracion) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        	if(oUsuario.isPresent()) {
        		Usuario usuario = oUsuario.get();
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
        	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @CrossOrigin
    @PostMapping(value = "/insertarCalificacionComentario")
    public ResponseEntity<?> insertarCalificacionComentario(@RequestParam("idUsuario") int idUsuario,
    		@RequestParam("idReceta") int idReceta, @RequestParam("comentario") String comentario) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        	if(oUsuario.isPresent()) {
        		Usuario usuario = oUsuario.get();
        		Calificacion calificacion = new Calificacion();
        		calificacion.setCalificacion(0);
        		calificacion.setComentarios(comentario);
        		calificacion.setReceta(receta);
        		calificacion.setUsuario(usuario);
        		calificacionService.save(calificacion);
        		receta.addCalificacion(calificacion);
        		recetaService.save(receta);
        		return ResponseEntity.ok().body(calificacion);	
        	}
        	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    
    @CrossOrigin
    @PostMapping(value = "/insertarCalificacionValoracion")
    public ResponseEntity<?> insertarCalificacionValoracion(@RequestParam("idUsuario") int idUsuario,
    		@RequestParam("idReceta") int idReceta, @RequestParam("valoracion") int valoracion) {
        Optional<Receta> oReceta = recetaService.findById(idReceta);
        if(oReceta.isPresent()) {
        	Receta receta = oReceta.get();
        	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
        	if(oUsuario.isPresent()) {
        		Usuario usuario = oUsuario.get();
        		List<Calificacion> calificaciones = receta.getCalificaciones().stream().filter(c -> c.getUsuario().equals(usuario)).toList();
            	calificaciones = calificaciones.stream().filter(c -> c.getCalificacion()>0).toList();
            	if (calificaciones.size()>0) {
            		Calificacion calificacion = calificaciones.get(0);
            		calificacion.setCalificacion(valoracion);
            		recetaService.save(receta);
            		return ResponseEntity.ok().body(valoracion);
            	}
            	else {
            		Calificacion calificacion = new Calificacion();
            		calificacion.setCalificacion(valoracion);
            		calificacion.setComentarios("");
            		calificacion.setReceta(receta);
            		calificacion.setUsuario(usuario);
            		calificacionService.save(calificacion);
            		receta.addCalificacion(calificacion);
            		recetaService.save(receta);
            		return ResponseEntity.ok().body(valoracion);
            	}
        	}
        	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
        }
        return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    }
    //TODO//
    @CrossOrigin
    @GetMapping(value = "getValoracionUsuarioReceta/{idUsuario}/{idReceta}")
    public ResponseEntity<?> getValoracionUsuarioReceta(@PathVariable Integer idUsuario,@PathVariable Integer idReceta){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		Optional<Receta> oReceta = recetaService.findById(idReceta);
            if(oReceta.isPresent()) {
            	Receta receta = oReceta.get();
            	List<Calificacion> calificaciones = receta.getCalificaciones().stream().filter(c -> c.getUsuario().equals(usuario)).toList();
            	int valoracionTotal = calificaciones.stream().mapToInt(Calificacion::getCalificacion).sum();
            	return ResponseEntity.ok().body(valoracionTotal);
            }
            return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
    }
    
    @CrossOrigin
    @GetMapping(value = "getUsuarioGuardoReceta/{idUsuario}/{idReceta}")
    public ResponseEntity<?> getUsuarioGuardoReceta(@PathVariable Integer idUsuario,@PathVariable Integer idReceta){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		Optional<Receta> oReceta = recetaService.findById(idReceta);
            if(oReceta.isPresent()) {
            	Receta receta = oReceta.get();
            	Boolean Contiene = usuario.getRecetasIntentar().contains(receta);
            	return ResponseEntity.ok().body(Contiene);
            }
            return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
    }
    
    @CrossOrigin
    @GetMapping(value = "getUsuarioGuardados/{idUsuario}")
    public ResponseEntity<?> getUsuarioGuardados(@PathVariable Integer idUsuario){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
            return ResponseEntity.ok().body(usuario.getRecetasIntentar());
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
    }
    
    @CrossOrigin
    @PostMapping(value = "setUsuarioGuardoReceta/{idUsuario}/{idReceta}")
    public ResponseEntity<?> setUsuarioGuardoReceta(@PathVariable Integer idUsuario,@PathVariable Integer idReceta){
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		Optional<Receta> oReceta = recetaService.findById(idReceta);
            if(oReceta.isPresent()) {
            	Receta receta = oReceta.get();
            	Boolean Contiene = usuario.getRecetasIntentar().contains(receta);
            	if(Contiene) {
            		usuario.getRecetasIntentar().remove(receta);
            		usuarioService.save(usuario);
            		return ResponseEntity.ok().body("Receta Eliminada");
            	}
            	else {
            		usuario.addRecetaIntentar(receta);
            		usuarioService.save(usuario);
            		return ResponseEntity.ok().body("Receta Guardada");
            	}
            	
            }
            return ResponseEntity.unprocessableEntity().body("Receta no Encontrada");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Encontrado");
    }
    
    @CrossOrigin
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
    
    @CrossOrigin
    @GetMapping(value = "/validarNombre/{nombreReceta}/{idUsuario}")
    public ResponseEntity<?> validarNombre(@PathVariable String nombreReceta,@PathVariable int idUsuario) {
    	Optional<Usuario> oUsuario = usuarioService.findUsuarioById(idUsuario);
    	if(oUsuario.isPresent()) {
    		Usuario usuario = oUsuario.get();
    		List<Receta> recetas = usuario.getRecetasCreadas();
    		List<Receta> recetasNombre = recetas.stream().filter(r -> r.getNombre().equals(nombreReceta)).toList();
    		boolean nombreUsado = 0<recetasNombre.size();
    		if(nombreUsado) {
    			return ResponseEntity.unprocessableEntity().body(recetasNombre.get(0));
    		}
    		return ResponseEntity.ok().body("Nombre Disponible");
    	}
    	return ResponseEntity.unprocessableEntity().body("Usuario no Valido");
    }
    
    @CrossOrigin
    @GetMapping(value = "/getIngredientes")
    public ResponseEntity<?> getIngredientes() {
    	return ResponseEntity.ok().body(ingredienteService.findAll());
    }
    
    @CrossOrigin
    @GetMapping(value = "/getCategorias")
    public ResponseEntity<?> getCategorias() {
    	return ResponseEntity.ok().body(tipoService.findAll());
    }
    
    @CrossOrigin
    @GetMapping(value = "/getUnidades")
    public ResponseEntity<?> getUnidad() {
    	return ResponseEntity.ok().body(unidadService.findAll());
    }
    
    @CrossOrigin
    @PostMapping(value = "/CargarReceta")
    public ResponseEntity<?> cargarReceta(@RequestParam("idUsuario") int idUsuario,
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
    
    
    
    
    @CrossOrigin
    @GetMapping(value = "/buscarReceta/{nombre}")
    public ResponseEntity<?> getRecetasByNombre(@PathVariable String nombre) {
        List<Receta> todasRecetas = recetaService.obtenerRecetasbyNombre(nombre);
        todasRecetas.sort(Comparator.comparing(Receta::getIdReceta).reversed());
        return ResponseEntity.ok().body(todasRecetas);
    }
    @CrossOrigin
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
    @CrossOrigin
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
    @CrossOrigin
    @GetMapping(value = "/recetasOrderByUsuario")
    public ResponseEntity<?> getRecetasOrderByUsuario() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        todasRecetas.sort(Comparator.comparing(Receta::getNombreUsuario));
        return ResponseEntity.ok().body(todasRecetas);
    }
    @CrossOrigin
    @GetMapping(value = "/recetasOrderByReceta")
    public ResponseEntity<?> getRecetasOrderByReceta() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        todasRecetas.sort(Comparator.comparing(Receta::getNombre));
        return ResponseEntity.ok().body(todasRecetas);
    }
    @CrossOrigin
    @GetMapping(value = "/recetasOrderByAntiguedad")
    public ResponseEntity<?> getRecetasOrderByAntiguedad() {
        List<Receta> todasRecetas = recetaService.obtenerTodosRecetas();
        return ResponseEntity.ok().body(todasRecetas);
    }
    @CrossOrigin
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
    @CrossOrigin
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
