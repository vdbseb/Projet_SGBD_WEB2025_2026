package be.angularpadelclub;

import be.angularpadelclub.Entity.CourtEntity;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.MemberType;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Repository.CourtRepository;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private SiteRepository siteRepository;

    @Autowired
    private CourtRepository courtRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    void shouldSaveReservation() {
        SiteEntity site = new SiteEntity();
        site.setNom("Atomium Club");
        site.setVille("Bruxelles");
        site.setActif(true);
        site = siteRepository.save(site);

        CourtEntity court = new CourtEntity();
        court.setName("Court 1");
        court.setCouvert(true);
        court.setActif(true);
        court.setSite(site);
        court = courtRepository.save(court);

        MemberEntity member = new MemberEntity();
        member.setMatricule("G1234");
        member.setPrenom("Vincent");
        member.setNom("Harmegnies");
        member.setEmail("vincent@example.com");
        member.setType(MemberType.GLOBAL);
        member.setActif(true);
        member.setSoldeDu(0.0);
        member.setPenaliteJours(0);
        member = memberRepository.save(member);

        ReservationEntity entity = new ReservationEntity();
        entity.setCourt(court);
        entity.setMember(member);
        entity.setDate(LocalDate.of(2026, 6, 20));
        entity.setStartTime(LocalTime.of(18, 0));
        entity.setEndTime(LocalTime.of(19, 30));

        ReservationEntity saved = reservationRepository.save(entity);

        assertNotNull(saved.getId());
    }
}