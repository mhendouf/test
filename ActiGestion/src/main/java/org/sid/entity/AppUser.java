package org.sid.entity;

import java.util.ArrayList;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.ToString;

@Entity
@ToString
public class AppUser {
	/**
	 * 
	 */

	public AppUser() {
		super ( );
	}

	private Boolean active;

	public AppUser(Long id, String username, String password, Collection<AppRoles> roles) {
		super ( );
		this.id = id;
		this.username = username;
		this.password = password;
		this.roles = roles;
	}

	/*
	 * public void activate() { this.active = true; }
	 * 
	 * public void deactivate() { this.active = false; }
	 */

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the username
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return the password
	 */
	@JsonIgnore
	public String getPassword() {
		return password;
	}

	/**
	 * @param password the password to set
	 */
	@JsonSetter
	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * @return the roles
	 */
	public Collection<AppRoles> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(Collection<AppRoles> roles) {
		this.roles = roles;
	}

	@Id
	@GeneratedValue
	private Long id;
	@Column(unique = true)
	private String username;
	// @JsonIgnore
	private String password;
	@ManyToMany(fetch = FetchType.EAGER)
	private Collection<AppRoles> roles = new ArrayList<> ( );
}
