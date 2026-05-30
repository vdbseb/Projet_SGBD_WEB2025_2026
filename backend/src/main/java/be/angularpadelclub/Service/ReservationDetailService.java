package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDetailDTO;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Entity.ReservationEntity;
import be.angularpadelclub.Mapper.ReservationDetailMapper;
import be.angularpadelclub.Repository.ParticipationRepository;
import be.angularpadelclub.Repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationDetailService {

    private final ParticipationRepository participationRepository;
    private final ReservationDetailMapper reservationDetailMapper;
    private final ReservationRepository reservationRepository;
    private final ReferenceLookupService referenceLookupService;

    public ReservationDetailService(
            ParticipationRepository participationRepository,
            ReservationDetailMapper reservationDetailMapper,
            ReservationRepository reservationRepository,
            ReferenceLookupService referenceLookupService
    ) {
        this.participationRepository = participationRepository;
        this.reservationDetailMapper = reservationDetailMapper;
        this.reservationRepository = reservationRepository;
        this.referenceLookupService = referenceLookupService;
    }

    public ReservationDetailDTO findByMatchId(
            Integer matchId,
            String currentMatricule
    ) {
        MatchEntity match = referenceLookupService.findMatchOrThrow(matchId);

        List<ParticipationEntity> participations =
                participationRepository.findByMatchId(matchId);

        return reservationDetailMapper.toDetailsDTO(
                match,
                participations,
                currentMatricule
        );
    }

    public List<ReservationEntity> findAll() {
        return reservationRepository.findAll();
    }
}