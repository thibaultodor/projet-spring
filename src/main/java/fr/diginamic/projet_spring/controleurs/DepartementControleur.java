package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.controleurs.interfaces.IDepartementControleur;
import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.interfaces.IDepartementService;
import fr.diginamic.projet_spring.services.interfaces.IExportService;
import fr.diginamic.projet_spring.services.interfaces.IVilleService;
import jakarta.validation.Valid;
import org.springframework.security.access.annotation.Secured;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

/** Expose le CRUD des départements et la consultation de leurs villes. */
@RestController
@RequestMapping("/departements")
public class DepartementControleur implements IDepartementControleur {

    // Le contrôleur utilise un contrat. Spring fournit la classe qui le réalise.
    private final IDepartementService departementService;
    private final IVilleService villeService;

    private final IExportService exportService;

    public DepartementControleur(IDepartementService departementService, IVilleService villeService,
                                 IExportService exportService) {
        this.departementService = departementService;
        this.villeService = villeService;
        this.exportService = exportService;
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping
    public List<DepartementDto> getDepartements() {
        return departementService.extractDepartements();
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{id}")
    public DepartementDto getDepartement(
            @PathVariable int id) throws ExceptionMetier {
        return departementService.extractDepartement(id);
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{id}/villes")
    public List<VilleDto> getVillesDuDepartement(
            @PathVariable int id) throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PostMapping
    public List<DepartementDto> ajouterDepartement(@Valid @RequestBody DepartementDto departement)
            throws ExceptionMetier {
        return departementService.insertDepartement(departement);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @PutMapping("/{id}")
    public List<DepartementDto> modifierDepartement(
            @PathVariable int id, @Valid @RequestBody DepartementDto departement)
            throws ExceptionMetier {
        return departementService.modifierDepartement(id, departement);
    }

    @Override
    @Secured("ROLE_ADMIN")
    @DeleteMapping("/{id}")
    public List<DepartementDto> supprimerDepartement(
            @PathVariable int id) throws ExceptionMetier {
        return departementService.supprimerDepartement(id);
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping("/{id}/villes/plus-grandes")
    public List<VilleDto> getPlusGrandesVilles(
            @PathVariable int id,
            @RequestParam(defaultValue = "10") int n)
            throws ExceptionMetier {
        return villeService.extractPlusGrandesVilles(id, n);
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/{id}/villes/population", params = "!max")
    public List<VilleDto> getVillesParPopulationMin(
            @PathVariable int id,
            @RequestParam int min)
            throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id, min);
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/{id}/villes/population", params = "max")
    public List<VilleDto> getVillesParPopulation(
            @PathVariable int id,
            @RequestParam int min,
            @RequestParam int max)
            throws ExceptionMetier {
        return villeService.extractVillesDuDepartement(id, min, max);
    }

    @Override
    @Secured({"ROLE_USER", "ROLE_ADMIN"})
    @GetMapping(value = "/{code}/export/pdf", produces = "application/pdf")
    public ResponseEntity<byte[]> exporterPdf(
            @PathVariable String code) throws ExceptionMetier {
        byte[] pdf = exportService.exporterDepartementPdf(code);
        String codeFichier = code.toUpperCase(java.util.Locale.ROOT).replaceAll("[^A-Z0-9]", "_");
        String fichier = ContentDisposition.attachment().filename("departement-" + codeFichier + ".pdf").build().toString();
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, fichier).body(pdf);
    }
}
