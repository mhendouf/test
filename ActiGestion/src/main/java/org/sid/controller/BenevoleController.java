package org.sid.controller;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.sid.Config.SecurityConstants;
import org.sid.entity.Benevole;
import org.sid.entity.ImageModel;
import org.sid.exception.RessourceNotFoundException;
import org.sid.repository.BenevoleRepository;
import org.sid.repository.ImageRepository;
import org.sid.service.FilesStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RestController
//@RequestMapping("api")
public class BenevoleController {
	@Autowired
	ImageRepository imageRepository;
	@Autowired
	BenevoleRepository repository;
	@Autowired
	ServletContext context;
	@Autowired
	FilesStorageService storageService;

	@GetMapping("/benevole")
	public List<Benevole> getBenevole(HttpServletRequest request) {
		// SecurityContextHolder.getContext ( ).getAuthentication ( ).setAuthenticated (
		// false );
		String jwt = request.getHeader ( SecurityConstants.HEADER_STRING );
		Object principal = SecurityContextHolder.getContext ( ).getAuthentication ( ).getPrincipal ( );
		String username;
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername ( );
		} else {
			username = principal.toString ( );
		}

		return repository.findByIdUser ( username );
	}

	@GetMapping("/benevole/all")
	public List<Benevole> getAllBenevole() {
		SecurityContextHolder.getContext ( ).getAuthentication ( ).setAuthenticated ( false );
		return repository.findAll ( );
	}

	/*
	 * @GetMapping("/benevole/imgprofile") public byte[] getPhoto() throws Exception
	 * { System.out.println (
	 * "IMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMMAAAAAAAAAAAAAAAAA "); Benevole b =
	 * repository.findById ( 81L ).get ( ); return Files.readAllBytes ( Paths.get (
	 * context.getRealPath ( "/Images/" ) +b.getImg_profile ( ) ) ); }
	 */
	@GetMapping("/benevole/imgprofile/{imageName}")
	public ImageModel getImage(@PathVariable("imageName") String imageName) throws IOException {
		final Optional<ImageModel> retrievedImage = imageRepository.findByName ( imageName );
		ImageModel img = new ImageModel ( retrievedImage.get ( ).getName ( ) , retrievedImage.get ( ).getType ( ) ,
				decompressBytes ( retrievedImage.get ( ).getPicByte ( ) ) );
		return img;
	}

	@GetMapping("/benevole/search/{nom}")
	public List<Benevole> getSearch(@PathVariable(value = "nom") String nom) throws RessourceNotFoundException {
		List benevoles = repository.findByNom ( nom );
		return benevoles;
	}

	@GetMapping("/benevole/{id}")
	public ResponseEntity<Benevole> getActiById(@PathVariable(value = "id") Long benevoleiId)
			throws RessourceNotFoundException {
		Benevole benevole = repository.findById ( benevoleiId )
				.orElseThrow ( () -> new RessourceNotFoundException ( "Benevole introuvable" ) );
		return ResponseEntity.ok ( ).body ( benevole );
	}

	/*
	 * @PutMapping("/pdf") public ResponseEntity<InputStreamResource>
	 * exportTermsPdf(@RequestBody List<Acti> actiId) { System.out.println (
	 * actiId.toString ( ) ); ByteArrayInputStream bais =
	 * exportActiService.actiPDFReport ( actiId ); HttpHeaders headers = new
	 * HttpHeaders ( ); return ResponseEntity.ok ( ).headers ( headers ).contentType
	 * ( MediaType.APPLICATION_PDF ) .body ( new InputStreamResource ( bais ) ); }
	 */

	/*
	 * @GetMapping("/pdf") public ResponseEntity<InputStreamResource> Pdf() {
	 * List<Acti> actiId = new ArrayList<Acti> ( ); ByteArrayInputStream bais =
	 * exportActiService.actiPDFReport ( actiId ); HttpHeaders headers = new
	 * HttpHeaders ( ); return ResponseEntity.ok ( ).headers ( headers ).contentType
	 * ( MediaType.APPLICATION_PDF ) .body ( new InputStreamResource ( bais ) ); }
	 */

	/*
	 * @PostMapping("/benevole") public Benevole AjouterBenevole(@RequestBody
	 * Benevole benevole) {
	 * 
	 * System.out.println ( benevole.toString ( ) );
	 * 
	 * return repository.save ( benevole ); }
	 */
	@PostMapping("/benevole")
	public Benevole AjouterBenevole(@RequestParam("benevole") String b, @RequestParam("file") MultipartFile file)
			throws JsonParseException, JsonMappingException, Exception {
		ImageModel img = new ImageModel ( file.getOriginalFilename ( ) , file.getContentType ( ) ,
				compressBytes ( file.getBytes ( ) ) );
		HttpServletRequest request;
		Benevole benevole = new ObjectMapper ( ).readValue ( b , Benevole.class );
		benevole.setDate_creation ( new Date ( ) );
		benevole = repository.save ( benevole );
		img.setName ( new Long ( benevole.getId ( ) ).toString ( ) );
		String root = new Long ( benevole.getId ( ) ).toString ( ) + benevole.getNom ( );
		storageService.initRoot ( root );
		benevole.setRoot ( root );
		benevole.setImg_profile ( img.getName ( ) );
		img = imageRepository.save ( img );
		return benevole;
	}

	public static byte[] compressBytes(byte[] data) {
		Deflater deflater = new Deflater ( );
		deflater.setInput ( data );
		deflater.finish ( );
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream ( data.length );
		byte[] buffer = new byte[1024];

		while (!deflater.finished ( )) {
			int count = deflater.deflate ( buffer );

			outputStream.write ( buffer , 0 , count );

		}

		try {
			outputStream.close ( );

		} catch (IOException e) {
		}

		System.out.println ( "Compressed Image Byte Size - " + outputStream.toByteArray ( ).length );

		return outputStream.toByteArray ( );

	}

	// uncompress the image bytes before returning it to the angular application

	public static byte[] decompressBytes(byte[] data) {

		Inflater inflater = new Inflater ( );

		inflater.setInput ( data );

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream ( data.length );

		byte[] buffer = new byte[1024];

		try {

			while (!inflater.finished ( )) {

				int count = inflater.inflate ( buffer );

				outputStream.write ( buffer , 0 , count );

			}

			outputStream.close ( );

		} catch (IOException ioe) {

		} catch (DataFormatException e) {

		}

		return outputStream.toByteArray ( );

	}

	@DeleteMapping("/benevole/{id}")
	public Map<String, Boolean> SupprimerActi(@PathVariable(value = "id") Long benevoleId)
			throws RessourceNotFoundException {
		Map<String, Boolean> map = new HashMap<> ( );
		try {
			Benevole benevole = repository.findById ( benevoleId )
					.orElseThrow ( () -> new RessourceNotFoundException ( "Client introuvable" ) );

			final Optional<ImageModel> retrievedImage = imageRepository.findByName ( benevole.getImg_profile ( ) );

			repository.delete ( benevole );
			imageRepository.deleteById ( retrievedImage.get ( ).getId ( ) );
			storageService.deleteAll ( benevole.getRoot ( ) );
			imageRepository.deleteById ( retrievedImage.get ( ).getId ( ) );

			map.put ( "Benevole Supprimé" , Boolean.TRUE );
		} catch (Exception e) {
			map.put ( "Erreur Benevole" , Boolean.FALSE );
		}

		return map;
	}

	@DeleteMapping("/benevole/delete")
	public ResponseEntity<String> SupprimerBenevole() {
		repository.deleteAll ( );
		return new ResponseEntity<> ( "Bénévoles supprimés" , HttpStatus.OK );
	}

	@PutMapping("/benevole/{id}")
	public ResponseEntity<Benevole> ModifierBenevole(@PathVariable(value = "id") long id,
			@RequestParam("benevole") String b, @RequestParam("file") MultipartFile file)
			throws JsonParseException, JsonMappingException, Exception {
		ImageModel img = new ImageModel ( file.getOriginalFilename ( ) , file.getContentType ( ) ,
				compressBytes ( file.getBytes ( ) ) );

		Benevole benevole = new ObjectMapper ( ).readValue ( b , Benevole.class );

		Optional<Benevole> benevoleInfo = repository.findById ( id );
		if (benevoleInfo.isPresent ( )) {
			/*
			 * Client ar = Clientinfo.get ( ); ar.setCode ( arti.getCode ( ) );
			 * ar.setLibelle ( arti.getLibelle ( ) ); ar.setFodec ( arti.getFodec ( ) );
			 * ar.setId_cat ( arti.getId_cat ( ) ); ar.setId_scat ( arti.getId_scat ( ) );
			 * ar.setPa ( arti.getPa ( ) ); ar.setPv ( arti.getPv ( ) ); ar.setTva (
			 * arti.getTva ( ) ); ar.setDc ( arti.getDc ( ) );
			 */
			img.setName ( new Long ( benevole.getId ( ) ).toString ( ) );
			benevole.setImg_profile ( img.getName ( ) );
			imageRepository.save ( img );
			return new ResponseEntity<> ( repository.save ( benevole ) , HttpStatus.OK );
		} else
			return new ResponseEntity<> ( HttpStatus.NOT_FOUND );
	}
}
