package org.hartford.binsure.repository;

import org.hartford.binsure.entity.UnderwriterDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UnderwriterDecisionRepository extends JpaRepository<UnderwriterDecision, Long> {
    List<UnderwriterDecision> findByApplication_Id(Long applicationId);
    Optional<UnderwriterDecision> findFirstByApplication_IdOrderByDecidedAtDesc(Long applicationId);
    List<UnderwriterDecision> findByUnderwriter_Id(Long underwriterId);
}
