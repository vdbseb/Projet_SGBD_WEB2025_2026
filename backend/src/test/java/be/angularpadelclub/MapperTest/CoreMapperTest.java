package be.angularpadelclub.MapperTest;

import be.angularpadelclub.Mapper.AdministrateurMapper;
import be.angularpadelclub.Mapper.DetteMembreMapper;
import be.angularpadelclub.Mapper.HoraireSiteMapper;
import be.angularpadelclub.Mapper.JourFermetureMapper;
import be.angularpadelclub.Mapper.MatchMapper;
import be.angularpadelclub.Mapper.MembreMapper;
import be.angularpadelclub.Mapper.PaiementMapper;
import be.angularpadelclub.Mapper.ReservationMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CoreMapperTest {

    @Test
    void reservationMapper_returnsEmptyListWhenInputIsNull() {
        ReservationMapper mapper = new ReservationMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void matchMapper_returnsNullWhenInputIsNull() {
        MatchMapper mapper = new MatchMapper();

        assertNull(mapper.toDTO(null));
    }

    @Test
    void paiementMapper_returnsEmptyListWhenInputIsNull() {
        PaiementMapper mapper = new PaiementMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void membreMapper_returnsEmptyListWhenInputIsNull() {
        MembreMapper mapper = new MembreMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void administrateurMapper_returnsEmptyListWhenInputIsNull() {
        AdministrateurMapper mapper = new AdministrateurMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void detteMembreMapper_returnsEmptyListWhenInputIsNull() {
        DetteMembreMapper mapper = new DetteMembreMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void horaireSiteMapper_returnsEmptyListWhenInputIsNull() {
        HoraireSiteMapper mapper = new HoraireSiteMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }

    @Test
    void jourFermetureMapper_returnsEmptyListWhenInputIsNull() {
        JourFermetureMapper mapper = new JourFermetureMapper();

        assertTrue(mapper.toDTOList(null).isEmpty());
    }
}