package com.practicasDeDesarrollo.backend.repository;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

@Repository
public class HealthRepository {

    private final DataSource dataSource;

    public HealthRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public boolean isDatabaseUp() throws Exception {
        try (
                Connection conn = dataSource.getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT 1");
                ResultSet rs = stmt.executeQuery()
        ) {
            return rs.next(); // si devuelve algo → DB OK
        }
    }
}
