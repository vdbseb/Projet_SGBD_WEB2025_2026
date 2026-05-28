package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MatchDTO;

import be.angularpadelclub.Mapper.MatchMapper;

import be.angularpadelclub.Service.MatchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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

    @GetMapping
    public List<MatchDTO> getAllMatches() {
        return matchService.findAll()
                .stream()
                .map(matchMapper::toDTO)
                .toList();
    }

//    @PostMapping
//    public MatchDTO createMatch(@RequestBody MatchDTO dto) {
//        return matchMapper.toDTO(
//                matchService.createMatch(dto)
//        );
//    }

    @PatchMapping("/{matchId}/annuler")
    public MatchDTO annulerMatch(
            @PathVariable Integer matchId,
            @RequestParam String matricule
    ) {
        return matchMapper.toDTO(
                matchService.annulerMatch(matchId, matricule)
        );
    }

    @PostMapping("/{matchId}/join/{memberId}")
    public ResponseEntity<Void> joinMatch(
            @PathVariable Integer matchId,
            @PathVariable Integer memberId
    ) {
        matchService.joinPublicMatch(
                matchId,
                memberId
        );

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{matchId}/leave/{memberId}")
    public ResponseEntity<Void> leaveMatch(
            @PathVariable Integer matchId,
            @PathVariable Integer memberId
    ) {
        matchService.leaveMatch(
                matchId,
                memberId
        );

        return ResponseEntity.noContent().build();
    }
}