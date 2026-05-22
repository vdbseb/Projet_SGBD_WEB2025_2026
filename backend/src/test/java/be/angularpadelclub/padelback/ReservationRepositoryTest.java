package be.angularpadelclub.padelback;

import be.angularpadelclub.padelback.court.CourtEntity;
import be.angularpadelclub.padelback.court.CourtRepository;
import be.angularpadelclub.padelback.member.MemberEntity;
import be.angularpadelclub.padelback.member.MemberRepository;
import be.angularpadelclub.padelback.member.MemberType;
import be.angularpadelclub.padelback.reservation.ReservationEntity;
import be.angularpadelclub.padelback.reservation.ReservationRepository;
import be.angularpadelclub.padelback.site.SiteEntity;
import be.angularpadelclub.padelback.site.SiteRepository;
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
        site.setName("Atomium Club");
        site.setCity("Bruxelles");
        site.setOpeningTime(LocalTime.of(8, 0));
        site.setClosingTime(LocalTime.of(23, 0));
        site.setActive(true);
        site = siteRepository.save(site);

        CourtEntity court = new CourtEntity();
        court.setName("Court 1");
        court.setType("Indoor");
        court.setSite(site);
        court = courtRepository.save(court);

        MemberEntity member = new MemberEntity();
        member.setMatricule("G1234");
        member.setFirstName("Vincent");
        member.setLastName("Harmegnies");
        member.setType(MemberType.GLOBAL);
        member = memberRepository.save(member);

        ReservationEntity entity = new ReservationEntity();
        entity.setCourt(court);
        entity.setMember(member);
        entity.setDate(LocalDate.of(2026, 6, 20));
        entity.setStartTime(LocalTime.of(18, 0));
        entity.setEndTime(LocalTime.of(19, 30));

        ReservationEntity saved = reservationRepository.save(entity);

        assertNotNull(saved.getId());
        assertNotNull(saved.getCreatedAt());
    }
}