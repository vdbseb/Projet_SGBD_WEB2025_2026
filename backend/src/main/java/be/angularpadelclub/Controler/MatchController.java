package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MatchDTO;
import be.angularpadelclub.Entity.ParticipationEntity;
import be.angularpadelclub.Mapper.MatchMapper;
import be.angularpadelclub.Service.MatchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matches")
@CrossOrigin(origins = "http://localhost:4200")
public class MatchController {

    private final MatchService matchService;
    private final MatchMapper matchMapper;

    public MatchController(
            MatchService matchService,
            MatchMapper matchMapper
    ) {
        this.matchService = matchService;
        this.matchMapper = matchMapper;
    }

    @GetMapping(produces = "application/json")
    public List<MatchDTO> getAllMatches() {
        return matchService.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }

    @PatchMapping(
            value = "/{matchId}/annuler",
            produces = "application/json"
    )
    public MatchDTO annulerMatch(
            @PathVariable("matchId") Integer matchId,
            @RequestParam("matricule") String matricule
    ) {
        return matchMapper.toDTO(
                matchService.annulerMatch(matchId, matricule)
        );
    }

    @PostMapping(
            path = "/{matchId}/join/{memberId}",
            produces = "application/json"
    )
    public Map<String, Integer> joinMatch(
            @PathVariable("matchId") Integer matchId,
            @PathVariable("memberId") Integer memberId
    ) {
        ParticipationEntity participation =
                matchService.joinPublicMatch(
                        matchId,
                        memberId
                );

        return Map.of(
                "participationId",
                participation.getId()
        );
    }

    @DeleteMapping("/{matchId}/leave/{memberId}")
    public void leaveMatch(
            @PathVariable("matchId") Integer matchId,
            @PathVariable("memberId") Integer memberId
    ) {
        matchService.leaveMatch(
                matchId,
                memberId
        );
    }
}