package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.controleurs.interfaces.IVilleControleur;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.interfaces.IVilleService;
import fr.diginamic.projet_spring.services.interfaces.IExportService;
import jakarta.validation.Valid;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.data.web.PagedModel;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Expose uniquement des VilleDto : les entités JPA restent dans les services et repositories. */
@RestController
@RequestMapping("/villes")
public class VilleControleur implements IVilleControleur {

    // Le contrôleur utilise un contrat. Spring fournit la classe qui le réalise.
    private final IVilleService villeService;

    private final IExportService exportService;

    public VilleControleur(IVilleService villeService, IExportService exportService) {
        this.villeService = villeService;
        this.exportService = exportService;
    }

    @Override
    @GetMapping
    public PagedModel<VilleDto> getVilles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) throws ExceptionMetier {
        // PagedModel fournit une réponse JSON simple : content et page.
        return new PagedModel<>(villeService.extractVillesPaginees(page, size));
    }

    @Override
    @GetMapping("/{id}")
    public VilleDto getVille(
            @PathVariable int id) throws ExceptionMetier {
        return villeService.extractVille(id);
    }

    @Override
    @PostMapping
    public List<VilleDto> ajouterVille(@Valid @RequestBody VilleDto ville) throws ExceptionMetier {
        return villeService.insertVille(ville);
    }

    @Override
    @PutMapping("/{id}")
    public List<VilleDto> modifierVille(
            @PathVariable int id, @Valid @RequestBody VilleDto nouvellesDonnees)
            throws ExceptionMetier {
        return villeService.modifierVille(id, nouvellesDonnees);
    }

    @Override
    @DeleteMapping("/{id}")
    public List<VilleDto> supprimerVille(
            @PathVariable int id) throws ExceptionMetier {
        return villeService.supprimerVille(id);
    }

    @Override
    @GetMapping("/recherche/nom")
    public List<VilleDto> rechercherParNom(
            @RequestParam String nom) throws ExceptionMetier {
        return villeService.extractVilles(nom);
    }

    @Override
    @GetMapping("/recherche/population")
    public List<VilleDto> rechercherParPopulationMin(
            @RequestParam int min) throws ExceptionMetier {
        return villeService.extractVilles(min);
    }

    @Override
    @GetMapping("/recherche/population/entre")
    public List<VilleDto> rechercherParPopulationEntre(
            @RequestParam int min,
            @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVilles(min, max);
    }

    @Override
    @GetMapping(value = "/export/csv", produces = "text/csv")
    public ResponseEntity<byte[]> exporterCsv(
            @RequestParam int min) {
        // attachment indique au navigateur qu'il s'agit d'un fichier à télécharger.
        String fichier = ContentDisposition.attachment().filename("villes-min-" + min + ".csv").build().toString();
        return ResponseEntity.ok().contentType(MediaType.parseMediaType("text/csv;charset=UTF-8"))
                .header(HttpHeaders.CONTENT_DISPOSITION, fichier).body(exportService.exporterVillesCsv(min));
    }
}
