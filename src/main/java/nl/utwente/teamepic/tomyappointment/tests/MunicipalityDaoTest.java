package nl.utwente.teamepic.tomyappointment.tests;

import nl.utwente.teamepic.tomyappointment.dao.MunicipalityDao;
import nl.utwente.teamepic.tomyappointment.model.Municipality;
import nl.utwente.teamepic.tomyappointment.model.Theme;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class MunicipalityDaoTest {
    MunicipalityDao dao;
    String municipalityId;

    @BeforeAll
    void beforeAll() throws SQLException, ClassNotFoundException {
        dao = new MunicipalityDao();
    }

    @BeforeEach
    void setUp() throws SQLException {
        Municipality municipality = new Municipality("a1a1a1a", "munitesting", "mutest");
        municipalityId = dao.insert(municipality);
    }

    @AfterEach
    void tearDown() throws SQLException {
        dao.delete(municipalityId);
    }

    @Test
    void getAll() throws SQLException {
        List<Municipality> municipalities = dao.getAll();
        for (Municipality municipality : municipalities) {
            if (municipality.getID().equals(municipalityId)) {
                assertTrue(true);
                break;
            }
        }
    }

    @Test
    void getById() throws SQLException {
        List<Municipality> municipalities = dao.getById(municipalityId);
        assertEquals(1, municipalities.size());
        assertEquals(municipalities.get(0).getID(), municipalityId);
    }

    @Test
    void insert() throws SQLException {
        Municipality municipality = new Municipality("a2a2a2a", "munitesting", "mutest");
        dao.insert(municipality);

        List<Municipality> municipalities = dao.getById(municipality.getID());
        assertEquals(1, municipalities.size());
        assertEquals(municipalities.get(0).getID(), municipality.getID());

        dao.delete(municipality.getID());
    }

    @Test
    void update() throws SQLException {
        List<Municipality> municipalities = dao.getById(municipalityId);
        assertEquals(municipalities.get(0).getID(), municipalityId);
        assertEquals("munitesting", municipalities.get(0).getName());

        Municipality municipality = new Municipality("a1a1a1a", "new munitesting", "mutest");
        dao.update(municipality);
        municipalities = dao.getById(municipalityId);
        assertEquals(municipalities.get(0).getID(), municipalityId);
        assertEquals("new munitesting", municipalities.get(0).getName());

        dao.delete(municipality.getID());
    }

    @Test
    void delete() throws SQLException {
        Municipality municipality = new Municipality("a4a4a4a", "munitesting", "mutest");
        String muniId = dao.insert(municipality);

        List<Municipality> municipalities = dao.getById(muniId);
        assertEquals(municipalities.get(0).getID(), muniId);

        dao.delete(muniId);

        municipalities = dao.getById(muniId);
        assertTrue(municipalities.isEmpty());
    }
}