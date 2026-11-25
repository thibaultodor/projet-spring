package fr.diginamic.projet_spring.services;

import fr.diginamic.projet_spring.dao.RegionDao;
import fr.diginamic.projet_spring.dto.DepartementApiDto;
import fr.diginamic.projet_spring.dto.DepartementDto;
import fr.diginamic.projet_spring.entites.Departement;
import fr.diginamic.projet_spring.entites.Region;
import fr.diginamic.projet_spring.exceptions.ExceptionMetier;
import fr.diginamic.projet_spring.mappers.DtoMapper;
import fr.diginamic.projet_spring.repositories.DepartementRepository;
import fr.diginamic.projet_spring.repositories.VilleRepository;
import fr.diginamic.projet_spring.services.interfaces.IDepartementService;
import jakarta.validation.Validator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles("test")
class DepartementServiceTest {

    @Autowired
    private IDepartementService departementService;

    @Autowired
    private Validator validator;

    @Autowired
    private DtoMapper dtoMapper;

    // Simule les accès à la base et à l'API externe.
    // Spring réinitialise les mocks entre les tests : aucune donnée n'est à supprimer.
    @MockitoBean
    private DepartementRepository departementRepository;

    @MockitoBean
    private VilleRepository villeRepository;

    @MockitoBean
    private RegionDao regionDao;

    @MockitoBean
    private RestTemplate restTemplate;

    private Region region;
    private Departement herault;

    @BeforeEach
    void preparerDonnees() {
        // Recrée les objets pour que les modifications d'un test ne touchent pas le suivant.
        region = new Region(10, "76", "Occitanie");
        herault = new Departement("34", "Hérault", region);
        herault.setId(1);
    }

    @Test
    void testExtractDepartements() {
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of(herault));

        List<DepartementDto> resultat = departementService.extractDepartements();

        assertEquals(1, resultat.size());
        assertEquals("Hérault", resultat.getFirst().getNom());
        assertEquals(10, resultat.getFirst().getIdRegion());
    }

    @Test
    void testExtractDepartementsListeVide() {
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertTrue(departementService.extractDepartements().isEmpty());
    }

    @Test
    void testExtractDepartement() throws ExceptionMetier {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));

        DepartementDto resultat = departementService.extractDepartement(1);

        assertEquals(1, resultat.getId());
        assertEquals("34", resultat.getCode());
        assertEquals("Hérault", resultat.getNom());
    }

    @Test
    void testExtractDepartementInconnu() {
        when(departementRepository.findById(99)).thenReturn(Optional.empty());

        ExceptionMetier erreur = assertThrows(ExceptionMetier.class,
                () -> departementService.extractDepartement(99));

        assertEquals(HttpStatus.NOT_FOUND, erreur.getStatut());
    }

    @Test
    void testInsertDepartementAvecRegion() throws ExceptionMetier {
        when(regionDao.extractRegion(10)).thenReturn(region);
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of(herault));

        List<DepartementDto> resultat = departementService.insertDepartement(
                new DepartementDto(0, " 34 ", " Hérault ", 10));

        // Vérifie les données réellement transmises au repository.
        ArgumentCaptor<Departement> capture = ArgumentCaptor.forClass(Departement.class);
        verify(departementRepository).saveAndFlush(capture.capture());
        assertEquals("34", capture.getValue().getCode());
        assertEquals("Hérault", capture.getValue().getNom());
        assertSame(region, capture.getValue().getRegion());
        assertEquals(1, resultat.size());
    }

    @Test
    void testInsertDepartementSansRegion() throws ExceptionMetier {
        departementService.insertDepartement(new DepartementDto(0, "2a", "Corse-du-Sud", null));

        ArgumentCaptor<Departement> capture = ArgumentCaptor.forClass(Departement.class);
        verify(departementRepository).saveAndFlush(capture.capture());
        assertEquals("2A", capture.getValue().getCode());
        assertNull(capture.getValue().getRegion());
        verifyNoInteractions(regionDao);
    }

    @Test
    void testInsertDepartementCodeDejaUtilise() {
        when(departementRepository.existsByCodeIgnoreCaseAndIdNot("34", 0)).thenReturn(true);

        assertThrows(ExceptionMetier.class, () -> departementService.insertDepartement(
                new DepartementDto(0, "34", "Hérault", null)));

        verify(departementRepository, never()).saveAndFlush(any());
    }

    @Test
    void testInsertDepartementDonneesInvalides() {
        assertThrows(ExceptionMetier.class, () -> departementService.insertDepartement(
                new DepartementDto(0, "3", "H", null)));

        verifyNoInteractions(departementRepository);
    }

    @Test
    void testModifierDepartementAvecNouvelleRegion() throws ExceptionMetier {
        Region nouvelleRegion = new Region(20, "94", "Corse");
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));
        when(regionDao.extractRegion(20)).thenReturn(nouvelleRegion);
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of(herault));

        List<DepartementDto> resultat = departementService.modifierDepartement(1,
                new DepartementDto(0, "2A", "Corse-du-Sud", 20));

        assertEquals(1, resultat.getFirst().getId());
        assertEquals("Corse-du-Sud", resultat.getFirst().getNom());
        assertEquals(20, resultat.getFirst().getIdRegion());
        verify(departementRepository).saveAndFlush(herault);
    }

    @Test
    void testModifierDepartementConserveRegion() throws ExceptionMetier {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));

        departementService.modifierDepartement(1, new DepartementDto(0, "34", "Nouveau nom", null));

        assertEquals("Nouveau nom", herault.getNom());
        assertSame(region, herault.getRegion());
        verifyNoInteractions(regionDao);
    }

    @Test
    void testModifierDepartementInconnu() {
        when(departementRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ExceptionMetier.class, () -> departementService.modifierDepartement(99,
                new DepartementDto(0, "34", "Hérault", null)));

        verify(departementRepository, never()).saveAndFlush(any());
    }

    @Test
    void testSupprimerDepartementSansVilles() throws ExceptionMetier {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));
        when(villeRepository.existsByDepartementId(1)).thenReturn(false);
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of());

        assertTrue(departementService.supprimerDepartement(1).isEmpty());

        verify(departementRepository).delete(herault);
        verify(departementRepository).flush();
    }

    @Test
    void testSupprimerDepartementAvecVilles() {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));
        when(villeRepository.existsByDepartementId(1)).thenReturn(true);

        ExceptionMetier erreur = assertThrows(ExceptionMetier.class,
                () -> departementService.supprimerDepartement(1));

        assertEquals(HttpStatus.CONFLICT, erreur.getStatut());
        verify(departementRepository, never()).delete(any());
    }

    @Test
    void testResoudreDepartementParId() throws ExceptionMetier {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));

        assertSame(herault, departementService.resoudreDepartement(1, null));
    }

    @Test
    void testResoudreDepartementParCode() throws ExceptionMetier {
        when(departementRepository.findByCodeIgnoreCase("34")).thenReturn(Optional.of(herault));

        assertSame(herault, departementService.resoudreDepartement(null, " 34 "));
    }

    @Test
    void testResoudreDepartementIdInconnuAvecCodeConnu() throws ExceptionMetier {
        when(departementRepository.findById(99)).thenReturn(Optional.empty());
        when(departementRepository.findByCodeIgnoreCase("34")).thenReturn(Optional.of(herault));

        assertSame(herault, departementService.resoudreDepartement(99, "34"));
    }

    @Test
    void testResoudreDepartementCreerCodeInconnu() throws ExceptionMetier {
        when(departementRepository.findByCodeIgnoreCase("Z9")).thenReturn(Optional.empty());

        Departement resultat = departementService.resoudreDepartement(null, "z9");

        verify(departementRepository).saveAndFlush(resultat);
        assertEquals("Z9", resultat.getCode());
        assertEquals("Département Z9", resultat.getNom());
        assertNull(resultat.getRegion());
    }

    @Test
    void testResoudreDepartementReferencesIncoherentes() {
        when(departementRepository.findById(1)).thenReturn(Optional.of(herault));

        assertThrows(ExceptionMetier.class, () -> departementService.resoudreDepartement(1, "2A"));
    }

    @Test
    void testResoudreDepartementSansReference() {
        assertThrows(ExceptionMetier.class, () -> departementService.resoudreDepartement(null, null));

        verifyNoInteractions(departementRepository);
    }

    @Test
    void testResoudreDepartementIdInconnuSansCode() {
        when(departementRepository.findById(99)).thenReturn(Optional.empty());

        ExceptionMetier erreur = assertThrows(ExceptionMetier.class,
                () -> departementService.resoudreDepartement(99, null));

        assertEquals(HttpStatus.NOT_FOUND, erreur.getStatut());
    }

    @Test
    void testInitDataDesactivee() throws ExceptionMetier {
        departementService.initData();

        verifyNoInteractions(restTemplate, departementRepository);
    }

    @Test
    void testInitDataMettreAJourLesNoms() throws ExceptionMetier {
        herault.setNom("Ancien nom");
        Departement local = new Departement("Z9", "Département local", null);
        when(restTemplate.getForObject(anyString(), eq(DepartementApiDto[].class)))
                .thenReturn(new DepartementApiDto[]{departementApi("34", "Hérault")});
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of(herault, local));

        serviceInitialisation().initData();

        assertEquals("Hérault", herault.getNom());
        assertEquals("34", herault.getCode());
        assertSame(region, herault.getRegion());
        assertEquals("Département local", local.getNom());
        verify(departementRepository).saveAllAndFlush(List.of(herault));
    }

    @Test
    void testInitDataNomsDejaAJour() throws ExceptionMetier {
        when(restTemplate.getForObject(anyString(), eq(DepartementApiDto[].class)))
                .thenReturn(new DepartementApiDto[]{departementApi("34", "Hérault")});
        when(departementRepository.findAllByOrderByIdAsc()).thenReturn(List.of(herault));

        serviceInitialisation().initData();

        verify(departementRepository, never()).saveAllAndFlush(anyList());
    }

    @Test
    void testInitDataApiIndisponible() {
        when(restTemplate.getForObject(anyString(), eq(DepartementApiDto[].class)))
                .thenThrow(new RestClientException("API indisponible"));

        ExceptionMetier erreur = assertThrows(ExceptionMetier.class, () -> serviceInitialisation().initData());

        assertEquals(HttpStatus.BAD_GATEWAY, erreur.getStatut());
        verifyNoInteractions(departementRepository);
    }

    @Test
    void testInitDataReponseVide() {
        when(restTemplate.getForObject(anyString(), eq(DepartementApiDto[].class)))
                .thenReturn(new DepartementApiDto[0]);

        assertThrows(ExceptionMetier.class, () -> serviceInitialisation().initData());

        verifyNoInteractions(departementRepository);
    }

    @Test
    void testInitDataDonneesInvalides() {
        when(restTemplate.getForObject(anyString(), eq(DepartementApiDto[].class)))
                .thenReturn(new DepartementApiDto[]{departementApi("34", null)});

        assertThrows(ExceptionMetier.class, () -> serviceInitialisation().initData());

        verifyNoInteractions(departementRepository);
    }

    private DepartementService serviceInitialisation() {
        // Active seulement cette instance : le démarrage Spring garde application.init=false.
        return new DepartementService(departementRepository, regionDao, villeRepository,
                validator, dtoMapper, restTemplate, true);
    }

    private DepartementApiDto departementApi(String code, String nom) {
        DepartementApiDto dto = new DepartementApiDto();
        dto.setCode(code);
        dto.setNom(nom);
        return dto;
    }
}
