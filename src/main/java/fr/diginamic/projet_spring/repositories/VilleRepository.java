package fr.diginamic.projet_spring.repositories;

import fr.diginamic.projet_spring.entites.Ville;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Spring Data construit les requêtes à partir du nom des méthodes. */
public interface VilleRepository extends JpaRepository<Ville, Integer> {

    // Charge aussi le département, utilisé pour construire chaque VilleDto.
    @Override
    @EntityGraph(attributePaths = "departement")
    Page<Ville> findAll(Pageable pagination);

    @Override
    @EntityGraph(attributePaths = "departement")
    List<Ville> findAll(Sort tri);

    @Override
    @EntityGraph(attributePaths = "departement")
    Optional<Ville> findById(Integer id);

    /** Recherche un début de nom sans distinguer les majuscules ; % et _ restent du texte. */
    @EntityGraph(attributePaths = "departement")
    List<Ville> findByNomStartingWithIgnoreCaseOrderByIdAsc(String prefixe);

    // GreaterThan et LessThan excluent les bornes. L'id départage les populations égales.
    @EntityGraph(attributePaths = "departement")
    List<Ville> findByPopulationGreaterThanOrderByPopulationDescIdAsc(int min);

    @EntityGraph(attributePaths = "departement")
    List<Ville> findByPopulationGreaterThanAndPopulationLessThanOrderByPopulationDescIdAsc(int min, int max);

    @EntityGraph(attributePaths = "departement")
    List<Ville> findByDepartementIdOrderByIdAsc(int idDepartement);

    @EntityGraph(attributePaths = "departement")
    List<Ville> findByDepartementIdAndPopulationGreaterThanOrderByPopulationDescIdAsc(int idDepartement, int min);

    @EntityGraph(attributePaths = "departement")
    List<Ville> findByDepartementIdAndPopulationGreaterThanAndPopulationLessThanOrderByPopulationDescIdAsc(
            int idDepartement, int min, int max);

    /** Pageable limite le nombre de villes directement en base, sans charger toute la liste. */
    @EntityGraph(attributePaths = "departement")
    List<Ville> findByDepartementIdOrderByPopulationDescIdAsc(int idDepartement, Pageable pagination);

    boolean existsByNomIgnoreCaseAndDepartementIdAndIdNot(String nom, int idDepartement, int idExclu);

    boolean existsByDepartementId(int idDepartement);
}
