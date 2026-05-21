package be.angularpadelclub.padelback.member;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/members")
@CrossOrigin(origins = "http://localhost:4200")
public class MemberController {

    private final MemberService memberService;
    private final MemberMapper memberMapper;

    public MemberController(
            MemberService memberService,
            MemberMapper memberMapper
    ) {
        this.memberService = memberService;
        this.memberMapper = memberMapper;
    }

    @GetMapping(produces = "application/json")
    public List<MemberDTO> findAll() {
        return memberMapper.toDTOList(memberService.findAll());
    }

    @GetMapping(value = "/{id}", produces = "application/json")
    public MemberDTO findById(@PathVariable UUID id) {
        return memberService.findById(id)
                .map(memberMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @PostMapping(consumes = "application/json")
    public void addMember(@RequestBody MemberDTO dto) {
        memberService.addMember(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable UUID id) {
        memberService.deleteMember(id);
    }

    @GetMapping(value = "/matricule/{matricule}", produces = "application/json")
    public MemberDTO findByMatricule(@PathVariable String matricule) {
        return memberService.findByMatricule(matricule)
                .map(memberMapper::toDTO)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Aucun membre trouvé avec ce matricule."
                ));
    }
}