package be.angularpadelclub.Controler;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @GetMapping
    public List<MemberDTO> getAllMembers() {
        return memberService.getAllMembers();
    }

    @GetMapping("/{matricule}")
    public MemberDTO getMemberByMatricule(@PathVariable String matricule) {
        return memberService.getMemberByMatricule(matricule);
    }

    @PostMapping
    public MemberDTO createMember(@RequestBody MemberDTO memberDTO) {
        return memberService.createMember(memberDTO);
    }

    @DeleteMapping("/{matricule}")
    public void deleteMember(@PathVariable String matricule) {
        memberService.deleteMember(matricule);
    }
}