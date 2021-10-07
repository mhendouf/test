package org.sid.controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.sid.Config.SecurityConstants;
import org.sid.entity.AppUser;
import org.sid.exception.RessourceNotFoundException;
import org.sid.repository.UserRepository;
import org.sid.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = "*")
@RestController
public class UserController {
	@Autowired
	private UserRepository repository;
	@Autowired
	private AccountService accountService;

	@PostMapping("/welcome")
	public String welcome(AppUser user) {
		System.out.println ( "USER CONTROLLE*************************" );
		String text = "this page is private page ";
		text += "this page is not allowed unauthenticated users";
		return text;
	}

	@GetMapping("/getusers")
	public List<AppUser> getAllUsers() {

		return repository.findAll ( );
	}

	@GetMapping("/user")
	public String getCurrentUser(HttpServletRequest request) {
		String jwt = request.getHeader ( SecurityConstants.HEADER_STRING );
		Object principal = SecurityContextHolder.getContext ( ).getAuthentication ( ).getPrincipal ( );
		String username;
		if (principal instanceof UserDetails) {
			username = ((UserDetails) principal).getUsername ( );
		} else {
			username = principal.toString ( );
		}
		return username;
	}

	@GetMapping("/user/{id}")
	public ResponseEntity<AppUser> getActiById(@PathVariable(value = "id") Long actiId)
			throws RessourceNotFoundException {
		AppUser user = repository.findById ( actiId )
				.orElseThrow ( () -> new RessourceNotFoundException ( "User introuvable" ) );
		return ResponseEntity.ok ( ).body ( user );
	}

	@PostMapping("/register")
	public AppUser AjouterUser(@RequestBody AppUser user) {
		// Date d = Calendar.getInstance ( ).getTime ( );
		accountService.saveUser ( user );
		accountService.addRolesToUser ( user.getUsername ( ) , "USER" );
		return user;
	}

	@DeleteMapping("/user/{id}")
	public Map<String, Boolean> SupprimerUser(@PathVariable(value = "id") Long userId)
			throws RessourceNotFoundException {
		AppUser user = repository.findById ( userId )
				.orElseThrow ( () -> new RessourceNotFoundException ( "User introuvable" ) );
		repository.delete ( user );
		Map<String, Boolean> map = new HashMap<> ( );
		map.put ( "User Supprimer" , Boolean.TRUE );
		return map;
	}

	@DeleteMapping("/user/delete")
	public ResponseEntity<String> SupprimerUsers() {
		repository.deleteAll ( );
		return new ResponseEntity<> ( "Users supprimes" , HttpStatus.OK );
	}

	@PutMapping("/user/{id}")
	public ResponseEntity<AppUser> ModifierActi(@PathVariable(value = "id") long id, @RequestBody AppUser user) {
		Optional<AppUser> Userinfo = repository.findById ( id );
		if (Userinfo.isPresent ( )) {
			/*
			 * Client ar = Clientinfo.get ( ); ar.setCode ( arti.getCode ( ) );
			 * ar.setLibelle ( arti.getLibelle ( ) ); ar.setFodec ( arti.getFodec ( ) );
			 * ar.setId_cat ( arti.getId_cat ( ) ); ar.setId_scat ( arti.getId_scat ( ) );
			 * ar.setPa ( arti.getPa ( ) ); ar.setPv ( arti.getPv ( ) ); ar.setTva (
			 * arti.getTva ( ) ); ar.setDc ( arti.getDc ( ) );
			 */
			return new ResponseEntity<> ( repository.save ( user ) , HttpStatus.OK );
		} else
			return new ResponseEntity<> ( HttpStatus.NOT_FOUND );
	}

}
