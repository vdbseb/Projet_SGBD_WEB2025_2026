package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.ReservationDetailDTO;
import be.angularpadelclub.Entity.MatchEntity;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Mapper.ReservationDetailMapper;
import be.angularpadelclub.Repository.MatchRepository;
import be.angularpadelclub.Repository.ParticipationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ReservationDetailService {

    private final MatchRepository matchRepository;
    private final ParticipationRepository participationRepository;
    private final ReservationDetailMapper reservationDetailMapper;

    public ReservationDetailService(
            MatchRepository matchRepository,
            ParticipationRepository participationRepository,
            ReservationDetailMapper reservationDetailMapper
    ) {
        this.matchRepository = matchRepository;
        this.participationRepository = participationRepository;
        this.reservationDetailMapper = reservationDetailMapper;
    }

    public ReservationDetailDTO findByMatchId(
            Integer matchId,
            String currentMatricule
    ) {
        MatchEntity match = matchRepository.findById(matchId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Match introuvable"
                ));

        List<ParticipationEntity> participations =
                participationRepository.findByMatchId(matchId);

        return reservationDetailMapper.toDetailsDTO(
                match,
                participations,
                currentMatricule
        );
    }
}