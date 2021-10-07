package org.sid.repository;

import java.util.List;

import org.sid.entity.Benevole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface BenevoleRepository extends JpaRepository<Benevole, Long> {
	@Query(value = "SELECT benevole FROM Benevole benevole where benevole.nom LIKE %?1%")
	List<Benevole> findByNom(String nom);

	@Query(value = "SELECT benevole FROM Benevole benevole Order by benevole.date_creation DESC")
	List<Benevole> findAll();

	@Query(value = "SELECT benevole FROM Benevole benevole where benevole.id_user LIKE %?1% ORDER BY benevole.date_creation DESC")
	List<Benevole> findByIdUser(String id_user);
}
