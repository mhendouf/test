package org.sid.controller;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.sid.Config.SecurityConstants;
import org.sid.entity.Acti;
import org.sid.entity.Benevole;
import org.sid.entity.Passage;
import org.sid.exception.RessourceNotFoundException;
import org.sid.repository.ActiRepository;
import org.sid.repository.PassageRepository;
import org.sid.service.ExportActiPdf;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;

@RestController
public class ActiController {
	@Autowired
	ActiRepository actiRepository;
	@Autowired
	ExportActiPdf exportPdfService;
	@Autowired
	PassageRepository passageRepository;

	@GetMapping("/acti")
	public List<Acti> getActis(HttpServletRequest request) {
		String jwt = request.getHeader ( SecurityConstants.HEADER_STRING );
		Object principal = SecurityContextHolder.getContext ( ).getAuthentication ( ).getPrincipal ( );
		String username;
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername ( );
		} else {
			username = principal.toString ( );
		}

		return actiRepository.findByIdUser ( username );
	}

	@GetMapping("/acti/all")
	public List<Acti> getAllActis() {

		return actiRepository.findAll ( );
	}

	@GetMapping("/acti/{id}")
	public ResponseEntity<Acti> getActiById(@PathVariable(value = "id") Long actiId) throws RessourceNotFoundException {
		Acti acti = actiRepository.findById ( actiId )
				.orElseThrow ( () -> new RessourceNotFoundException ( "Acti introuvable" ) );
		return ResponseEntity.ok ( ).body ( acti );
	}

	@GetMapping("/acti/search/{titre}")
	public List<Acti> getSearch(@PathVariable(value = "titre") String titre) throws RessourceNotFoundException {
		// System.out.println ( actiRepository.findByTitre ( titre ) );
		List acti = actiRepository.findByTitre ( titre );
		return acti;
	}

	@PostMapping("/acti")
	public Acti AjouterActi(@RequestBody Acti info) {
		info.getDate_acti ( ).setHours ( info.getDate_acti ( ).getHours ( ) - 2 );
		info.getDate_acti_fin ( ).setHours ( info.getDate_acti_fin ( ).getHours ( ) - 2 );
		info.setDate_creation ( new Date ( ) );
		return actiRepository.save ( (Acti) info );
	}

	@DeleteMapping("/acti/{id}")
	public Map<String, Boolean> SupprimerActi(@PathVariable(value = "id") Long actiId)
			throws RessourceNotFoundException {

		Acti acti = actiRepository.findById ( actiId )
				.orElseThrow ( () -> new RessourceNotFoundException ( "Acti introuvable" ) );

		actiRepository.delete ( acti );
		Map<String, Boolean> map = new HashMap<> ( );
		map.put ( "Acti Supprimé" , Boolean.TRUE );
		return map;
	}

	@DeleteMapping("/acti/delete")
	public ResponseEntity<String> SupprimerBenevole() {

		actiRepository.deleteAll ( );

		return new ResponseEntity<> ( "Actis supprimés" , HttpStatus.OK );
	}

	@PutMapping("/acti/{id}")
	public ResponseEntity<Acti> ModifierActi(@PathVariable(value = "id") long id, @RequestBody Acti ac)
			throws JsonMappingException, JsonProcessingException, RessourceNotFoundException {

		Acti actiInfo = actiRepository.findById ( id )
				.orElseThrow ( () -> new RessourceNotFoundException ( "Acti introuvable" ) );
		if (actiInfo.getId ( ) != null) {
			if (ac.getTitre ( ) == null) {
				ac.setTitre ( actiInfo.getTitre ( ) );
			}
			if (ac.getTitle ( ) == null) {
				ac.setTitle ( actiInfo.getTitle ( ) );
			}
			if (ac.getDescription ( ) == null) {
				ac.setDescription ( actiInfo.getDescription ( ) );
			}
			if (ac.getParticipants ( ) == null) {
				ac.setParticipants ( actiInfo.getParticipants ( ) );
			}
			if (ac.getDate_creation ( ) == null) {
				ac.setDate_creation ( actiInfo.getDate_creation ( ) );
			}
			if (ac.getDate_acti ( ) == null) {
				ac.setDate_acti ( actiInfo.getDate_acti ( ) );
			} else {
				// ac.getDate_acti ( ).setHours ( ac.getDate_acti ( ).getHours ( ) - 2 );
			}
			if (ac.getDate_acti_fin ( ) == null) {
				ac.setDate_acti_fin ( actiInfo.getDate_acti_fin ( ) );
			} else {
				// ac.getDate_acti_fin ( ).setHours ( ac.getDate_acti_fin ( ).getHours ( ) - 2
				// );
			}
			if (ac.getRepetition ( ) == null) {
				ac.setRepetition ( actiInfo.getRepetition ( ) );
			}
			if (ac.getId_user ( ) == null) {
				ac.setId_user ( actiInfo.getId_user ( ) );
			}
			if (ac.getNom_animateur ( ) == null) {
				ac.setNom_animateur ( actiInfo.getNom_animateur ( ) );
			}
			if (ac.getIsDone ( ) == null) {
				ac.setIsDone ( actiInfo.getIsDone ( ) );
			}
			if (ac.getBenevoles_list ( ).size ( ) < 1) {
				ac.setBenevoles_list ( actiInfo.getBenevoles_list ( ) );
			}
			return new ResponseEntity<> ( actiRepository.save ( ac ) , HttpStatus.OK );
		} else
			return new ResponseEntity<> ( HttpStatus.NOT_FOUND );
	}

	@GetMapping("/acti/valide/{id}")
	public boolean ValideActi(@PathVariable(value = "id") Long id) {
		Optional<Acti> actiInfo = actiRepository.findById ( id );
		Acti acti = actiInfo.get ( );
		Collection<Benevole> listBenevole = acti.getBenevoles_list ( );
		Date now = new Date ( );
		for (Benevole b : listBenevole) {
			Passage p = new Passage ( null , now , b.getId ( ) , acti.getId ( ) );
			passageRepository.save ( p );
		}
		acti.setIsDone ( "1" );
		actiRepository.save ( acti );
		return true;
	}

	@PutMapping("/acti/pdf")
	public ResponseEntity<InputStreamResource> exportPdf(@RequestBody List<Acti> actis) {
		for (Acti s : actis) {
		}
		ByteArrayInputStream bais = exportPdfService.actiPDFreport ( actis );
		HttpHeaders headers = new HttpHeaders ( );
		headers.add ( "Content-Disposition" , "filename=Actis.pdf" );
		return ResponseEntity.ok ( ).headers ( headers ).contentType ( MediaType.APPLICATION_PDF )
				.body ( new InputStreamResource ( bais ) );
	}

	@GetMapping("/acti/benevoles/{idBenevole}")
	public List<Acti> getAllActisBenevole(@PathVariable(value = "idBenevole") Long idBenevole) {
		// List<Acti> listActi = actiRepository.findByIdBenevole ( idBenevole );
		return actiRepository.findAll ( );
	}
}
