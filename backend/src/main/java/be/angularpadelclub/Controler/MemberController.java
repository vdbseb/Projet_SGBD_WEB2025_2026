package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Mapper.MemberMapper;
import be.angularpadelclub.Service.MemberService;
import org.springframework.web.bind.annotation.*;

import java.util.List;


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

    @GetMapping(value = "/{matricule}", produces = "application/json")
    public MemberDTO findById(@PathVariable String matricule) {
        return memberService.findByMatricule(matricule)
                .map(memberMapper::toDTO)
                .orElseThrow(() -> new RuntimeException("Member not found"));
    }

    @PostMapping(consumes = "application/json")
    public void addMember(@RequestBody MemberDTO dto) {
        memberService.addMember(dto);
    }

    @DeleteMapping("/{id}")
    public void deleteMember(@PathVariable int id) {
        memberService.deleteMember(id);
    }
}