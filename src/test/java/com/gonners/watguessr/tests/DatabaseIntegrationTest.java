package com.gonners.watguessr.tests;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Optional;

import com.gooners.watguessr.WatGuessr;
import com.gooners.watguessr.entity.Building;
import com.gooners.watguessr.repository.BuildingRepository;
import com.gonners.watguessr.config.ContainerDataSourceConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = WatGuessr.class)
@Import(ContainerDataSourceConfig.class)
@Transactional
public class DatabaseIntegrationTest {

    @Autowired
    private BuildingRepository buildingRepository;

    @BeforeAll
    static void beforeAll() throws Exception {
        try (Connection connection = java.sql.DriverManager.getConnection(
                ContainerDataSourceConfig.getPostgresContainer().getJdbcUrl(), 
                ContainerDataSourceConfig.getPostgresContainer().getUsername(), 
                ContainerDataSourceConfig.getPostgresContainer().getPassword());
             Statement statement = connection.createStatement()) {
            statement.execute("CREATE SCHEMA IF NOT EXISTS watguessr");
        }
    }

    @AfterAll
    static void afterAll() {
        ContainerDataSourceConfig.getPostgresContainer().stop();
    }

    @Test
    void singleTest_canConnectAndQuery() throws Exception {
        try (Connection connection = java.sql.DriverManager.getConnection(
                ContainerDataSourceConfig.getPostgresContainer().getJdbcUrl(), 
                ContainerDataSourceConfig.getPostgresContainer().getUsername(), 
                ContainerDataSourceConfig.getPostgresContainer().getPassword());
             PreparedStatement ps = connection.prepareStatement("SELECT 1");
             ResultSet rs = ps.executeQuery()) {
            assertTrue(rs.next());
            assertEquals(1, rs.getInt(1));
        }
    }

    @Test
    void testBuildingRepository_saveAndFindById() {
        // Given - Create a test building
        Building building = new Building();
        building.setName("Mathematics & Computer Building");
        building.setFloors(Arrays.asList(1, 2, 3, 4));
        building.setLatitude(new BigDecimal("43.472081"));
        building.setLongitude(new BigDecimal("-80.544962"));

        // When - Save the building
        Building savedBuilding = buildingRepository.save(building);

        // Then - Verify it was saved and can be retrieved
        assertTrue(savedBuilding.getId() != null);
        assertEquals("Mathematics & Computer Building", savedBuilding.getName());

        Optional<Building> foundBuilding = buildingRepository.findById(savedBuilding.getId());
        assertTrue(foundBuilding.isPresent());
        assertEquals("Mathematics & Computer Building", foundBuilding.get().getName());
        assertEquals(Arrays.asList(1, 2, 3, 4), foundBuilding.get().getFloors());
    }
} 