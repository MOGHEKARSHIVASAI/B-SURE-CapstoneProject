package org.hartford.binsure.repository;

import org.hartford.binsure.entity.Claim;
import org.hartford.binsure.enums.ClaimStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClaimRepository extends JpaRepository<Claim, Long> {
    Optional<Claim> findByClaimNumber(String claimNumber);

    List<Claim> findByBusiness_User_Id(Long userId);

    List<Claim> findByAssignedOfficer_Id(Long officerId);

    List<Claim> findByStatus(ClaimStatus status);

    List<Claim> findByAssignedOfficerIsNull();
}
