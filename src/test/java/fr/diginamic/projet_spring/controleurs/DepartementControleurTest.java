package fr.diginamic.projet_spring.controleurs;

import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.dto.VilleDto;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.services.interfaces.IDepartementService;
import fr.diginamic.projet_spring.services.interfaces.IExportService;
import fr.diginamic.projet_spring.services.interfaces.IVilleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// Démarre Spring avec H2. MockMvc appelle les routes sans lancer de serveur HTTP.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser(roles = {"USER", "ADMIN"})
class DepartementControleurTest {

    private static final String JSON_AVEC_REGION = """
            {"code":"34","nom":"Hérault","idRegion":10}
            """;
    private static final String JSON_SANS_REGION = """
            {"code":"2A","nom":"Corse-du-Sud"}
            """;

    @Autowired
    private MockMvc mockMvc;

    // Spring remplace les services par des mocks et les réinitialise entre les tests.
    @MockitoBean
    private IDepartementService departementService;

    @MockitoBean
    private IVilleService villeService;

    @MockitoBean
    private IExportService exportService;

    private DepartementDto herault;
    private DepartementDto corse;
    private VilleDto montpellier;
    private VilleDto sete;

    @BeforeEach
    void preparerDonnees() {
        // Prépare des réponses de service. Aucune donnée n'est insérée en base.
        herault = new DepartementDto(1, "34", "Hérault", 10);
        corse = new DepartementDto(2, "2A", "Corse-du-Sud", null);
        montpellier = new VilleDto(11, "Montpellier", 300000, 1, "34");
        sete = new VilleDto(12, "Sète", 45000, 1, "34");
    }

    @Test
    void testGetDepartements() throws Exception {
        when(departementService.extractDepartements()).thenReturn(List.of(herault, corse));

        mockMvc.perform(get("/departements")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nom").value("Hérault"));
    }

    @Test
    void testGetDepartementsListeVide() throws Exception {
        when(departementService.extractDepartements()).thenReturn(List.of());

        mockMvc.perform(get("/departements")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetDepartementAvecRegion() throws Exception {
        when(departementService.extractDepartement(1)).thenReturn(herault);

        mockMvc.perform(get("/departements/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("34"))
                .andExpect(jsonPath("$.idRegion").value(10));
    }

    @Test
    void testGetDepartementSansRegion() throws Exception {
        when(departementService.extractDepartement(2)).thenReturn(corse);

        mockMvc.perform(get("/departements/2")).andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("2A"))
                .andExpect(jsonPath("$.idRegion").value(nullValue()));
    }

    @Test
    void testGetVillesDuDepartement() throws Exception {
        when(villeService.extractVillesDuDepartement(1)).thenReturn(List.of(montpellier, sete));

        mockMvc.perform(get("/departements/1/villes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[*].nom", contains("Montpellier", "Sète")));
    }

    @Test
    void testGetVillesDuDepartementListeVide() throws Exception {
        when(villeService.extractVillesDuDepartement(2)).thenReturn(List.of());

        mockMvc.perform(get("/departements/2/villes")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
        verify(villeService).extractVillesDuDepartement(2);
    }

    @Test
    void testAjouterDepartementAvecRegion() throws Exception {
        when(departementService.insertDepartement(any(DepartementDto.class))).thenReturn(List.of(herault));

        mockMvc.perform(post("/departements").contentType(MediaType.APPLICATION_JSON).content(JSON_AVEC_REGION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].code").value("34"));

        // Vérifie que le JSON reçu a bien été converti en DTO.
        ArgumentCaptor<DepartementDto> capture = ArgumentCaptor.forClass(DepartementDto.class);
        verify(departementService).insertDepartement(capture.capture());
        assertEquals("Hérault", capture.getValue().getNom());
        assertEquals(10, capture.getValue().getIdRegion());
    }

    @Test
    void testAjouterDepartementSansRegion() throws Exception {
        when(departementService.insertDepartement(any(DepartementDto.class))).thenReturn(List.of(corse));

        mockMvc.perform(post("/departements").contentType(MediaType.APPLICATION_JSON).content(JSON_SANS_REGION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].code").value("2A"));

        ArgumentCaptor<DepartementDto> capture = ArgumentCaptor.forClass(DepartementDto.class);
        verify(departementService).insertDepartement(capture.capture());
        assertNull(capture.getValue().getIdRegion());
    }

    @Test
    void testModifierDepartementAvecRegion() throws Exception {
        when(departementService.modifierDepartement(eq(1), any(DepartementDto.class))).thenReturn(List.of(herault));

        mockMvc.perform(put("/departements/1").contentType(MediaType.APPLICATION_JSON).content(JSON_AVEC_REGION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(1));

        ArgumentCaptor<DepartementDto> capture = ArgumentCaptor.forClass(DepartementDto.class);
        verify(departementService).modifierDepartement(eq(1), capture.capture());
        assertEquals("34", capture.getValue().getCode());
        assertEquals(10, capture.getValue().getIdRegion());
    }

    @Test
    void testModifierDepartementSansRegion() throws Exception {
        when(departementService.modifierDepartement(eq(2), any(DepartementDto.class))).thenReturn(List.of(corse));

        mockMvc.perform(put("/departements/2").contentType(MediaType.APPLICATION_JSON).content(JSON_SANS_REGION))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nom").value("Corse-du-Sud"));
    }

    @Test
    void testSupprimerDepartementAvecAutresDepartementsRestants() throws Exception {
        when(departementService.supprimerDepartement(1)).thenReturn(List.of(corse));

        mockMvc.perform(delete("/departements/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1))).andExpect(jsonPath("$[0].code").value("2A"));
        verify(departementService).supprimerDepartement(1);
    }

    @Test
    void testSupprimerDernierDepartement() throws Exception {
        when(departementService.supprimerDepartement(1)).thenReturn(List.of());

        mockMvc.perform(delete("/departements/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testGetPlusGrandesVillesAvecN() throws Exception {
        when(villeService.extractPlusGrandesVilles(1, 1)).thenReturn(List.of(montpellier));

        mockMvc.perform(get("/departements/1/villes/plus-grandes").param("n", "1"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nom").value("Montpellier"));
    }

    @Test
    void testGetPlusGrandesVillesAvecNParDefaut() throws Exception {
        when(villeService.extractPlusGrandesVilles(1, 10)).thenReturn(List.of(montpellier, sete));

        mockMvc.perform(get("/departements/1/villes/plus-grandes")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
        verify(villeService).extractPlusGrandesVilles(1, 10);
    }

    @Test
    void testGetVillesParPopulationMin() throws Exception {
        when(villeService.extractVillesDuDepartement(1, 20000)).thenReturn(List.of(montpellier, sete));

        mockMvc.perform(get("/departements/1/villes/population").param("min", "20000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
        verify(villeService).extractVillesDuDepartement(1, 20000);
    }

    @Test
    void testGetVillesParPopulationMinSansResultat() throws Exception {
        when(villeService.extractVillesDuDepartement(1, 400000)).thenReturn(List.of());

        mockMvc.perform(get("/departements/1/villes/population").param("min", "400000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        verify(villeService).extractVillesDuDepartement(1, 400000);
    }

    @Test
    void testGetVillesParPopulationEntreBornes() throws Exception {
        when(villeService.extractVillesDuDepartement(1, 20000, 100000)).thenReturn(List.of(sete));

        mockMvc.perform(get("/departements/1/villes/population").param("min", "20000").param("max", "100000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].nom").value("Sète"));
        verify(villeService).extractVillesDuDepartement(1, 20000, 100000);
    }

    @Test
    void testGetVillesParPopulationEntreBornesSansResultat() throws Exception {
        when(villeService.extractVillesDuDepartement(1, 500000, 900000)).thenReturn(List.of());

        mockMvc.perform(get("/departements/1/villes/population").param("min", "500000").param("max", "900000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(0)));
        verify(villeService).extractVillesDuDepartement(1, 500000, 900000);
    }

    @Test
    void testExporterPdfAvecCodeNumerique() throws Exception {
        // Le contenu est simulé : ce test vérifie le téléchargement, pas la création du PDF.
        byte[] pdf = "Fichier de test".getBytes(StandardCharsets.UTF_8);
        when(exportService.exporterDepartementPdf("34")).thenReturn(pdf);

        mockMvc.perform(get("/departements/34/export/pdf")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string("Content-Disposition", containsString("departement-34.pdf")))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void testExporterPdfAvecCodeCorse() throws Exception {
        byte[] pdf = "Fichier de test corse".getBytes(StandardCharsets.UTF_8);
        when(exportService.exporterDepartementPdf("2a")).thenReturn(pdf);

        mockMvc.perform(get("/departements/2a/export/pdf")).andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("departement-2A.pdf")))
                .andExpect(content().bytes(pdf));
    }

    @Test
    void testGetDepartementInconnu() throws Exception {
        when(departementService.extractDepartement(99))
                .thenThrow(new ExceptionMetier("Département introuvable", HttpStatus.NOT_FOUND));

        mockMvc.perform(get("/departements/99")).andExpect(status().isNotFound())
                .andExpect(content().string("Département introuvable"));
    }

    @Test
    void testAjouterDepartementInvalide() throws Exception {
        mockMvc.perform(post("/departements").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"3","nom":"H"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.code").exists());

        verify(departementService, never()).insertDepartement(any());
    }

    @Test
    void testModifierDepartementInvalide() throws Exception {
        mockMvc.perform(put("/departements/1").contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"code":"34"}
                                """))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.nom").exists());

        verify(departementService, never()).modifierDepartement(anyInt(), any());
    }

    @Test
    void testSupprimerDepartementAvecVilles() throws Exception {
        when(departementService.supprimerDepartement(1)).thenThrow(new ExceptionMetier(
                "Impossible de supprimer un département qui contient des villes", HttpStatus.CONFLICT));

        mockMvc.perform(delete("/departements/1")).andExpect(status().isConflict());
    }
}
