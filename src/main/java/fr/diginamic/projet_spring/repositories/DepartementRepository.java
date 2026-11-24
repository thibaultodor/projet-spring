package fr.diginamic.projet_spring.repositories;

import fr.diginamic.projet_spring.entites.Departement;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import fr.diginamic.projet_spring.repositories.projections.StatistiquesDepartement;

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

    /** Additionne les populations par département directement en base pour le rapport comparatif. */
    @Query("""
            select d.id as id, d.code as code, d.nom as nom, r.id as idRegion,
                   count(v.id) as nombreVilles, coalesce(sum(v.population), 0) as population
            from Departement d left join d.villes v left join d.region r
            group by d.id, d.code, d.nom, r.id
            """)
    List<StatistiquesDepartement> extraireStatistiquesRapport();
}
