package be.angularpadelclub.Service;

import be.angularpadelclub.DTO.MemberDTO;
import be.angularpadelclub.Entity.MemberEntity;
import be.angularpadelclub.Entity.SiteEntity;
import be.angularpadelclub.Mapper.MemberMapper;
import be.angularpadelclub.Repository.MemberRepository;
import be.angularpadelclub.Repository.SiteRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final SiteRepository siteRepository;
    private final MemberMapper memberMapper;

    public MemberService(
            MemberRepository memberRepository,
            SiteRepository siteRepository,
            MemberMapper memberMapper
    ) {
        this.memberRepository = memberRepository;
        this.siteRepository = siteRepository;
        this.memberMapper = memberMapper;
    }

    public List<MemberEntity> findAll() {
        return memberRepository.findAll();
    }

    public Optional<MemberEntity> findById(int id) {
        return memberRepository.findById(id);
    }

    public Optional<MemberEntity> findByMatricule(String matricule) {
        return memberRepository.findByMatricule(matricule);
    }

    public void addMember(MemberDTO dto) {

        if (memberRepository.existsByMatricule(dto.matricule())) {
            throw new RuntimeException("Matricule already exists.");
        }

        SiteEntity site = null;

        if (dto.siteId() != null) {
            site = siteRepository.findById(dto.siteId())
                    .orElseThrow(() -> new RuntimeException("Site not found"));
        }

        MemberEntity member = memberMapper.toEntity(dto, site);

        member.setId(null);

        memberRepository.save(member);
    }

    public void deleteMember(int id) {
        memberRepository.deleteById(id);
    }
}