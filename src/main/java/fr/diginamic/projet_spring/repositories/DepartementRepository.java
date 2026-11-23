package fr.diginamic.projet_spring.repositories;

import fr.diginamic.projet_spring.entites.Departement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Remplace le DAO : Spring fournit les opérations CRUD grâce à JpaRepository. */
public interface DepartementRepository extends JpaRepository<Departement, Integer> {

    // La région est chargée avec le département, même lorsqu'elle est absente.
    @EntityGraph(attributePaths = "region")
    List<Departement> findAllByOrderByIdAsc();

    @Override
    @EntityGraph(attributePaths = "region")
    Optional<Departement> findById(Integer id);

    @EntityGraph(attributePaths = "region")
    Optional<Departement> findByCodeIgnoreCase(String code);

    boolean existsByCodeIgnoreCaseAndIdNot(String code, int idExclu);
}
