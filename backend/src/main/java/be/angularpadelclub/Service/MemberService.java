package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.MemberMapper;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final SiteRepository siteRepository;

    public List<MemberDTO> getAllMembers() {
        return memberRepository.findAll()
                .stream()
                .map(MemberMapper::toDTO)
                .collect(Collectors.toList());
    }

    public MemberDTO getMemberByMatricule(String matricule) {
        return memberRepository.findById(matricule)
                .map(MemberMapper::toDTO)
                .orElse(null);
    }

    public MemberDTO createMember(MemberDTO dto) {

        if (memberRepository.existsByMatricule(dto.getMatricule())) {
            throw new RuntimeException("Matricule already exists");
        }

        if (memberRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("Email already exists");
        }

        SiteEntity site = null;

        if (dto.getSiteId() != null) {
            site = siteRepository.findById(dto.getSiteId())
                    .orElseThrow(() -> new RuntimeException("Site not found"));
        }

        MemberEntity member = MemberMapper.toEntity(dto, site);

        return MemberMapper.toDTO(
                memberRepository.save(member)
        );
    }

    public void deleteMember(String matricule) {
        memberRepository.deleteById(matricule);
    }
}