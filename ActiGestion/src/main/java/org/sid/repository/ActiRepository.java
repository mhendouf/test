package org.sid.repository;

import java.util.List;

import org.sid.entity.Acti;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;

@RepositoryRestResource
public interface ActiRepository extends JpaRepository<Acti, Long> {
	@Query(value = "SELECT acti FROM Acti acti where acti.titre LIKE %?1%")
	List<Acti> findByTitre(String titre);

	/*
	 * @Query(value =
	 * "SELECT acti FROM Acti acti join acti.benevoles_list b where b.id =: id ")
	 * List<Acti> findByIdBenevole(Long id);
	 */
	@Query(value = "SELECT acti FROM Acti acti ORDER BY date_acti DESC")
	List<Acti> findAll();

	@Query(value = "SELECT acti FROM Acti acti where acti.id_user LIKE %?1% ORDER BY date_acti DESC")
	List<Acti> findByIdUser(String id_user);
}
