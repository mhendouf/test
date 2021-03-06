package org.sid.repository;

import org.sid.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface UserRepository extends JpaRepository<AppUser, Long> {

	public AppUser findByUsername(String username);

}
