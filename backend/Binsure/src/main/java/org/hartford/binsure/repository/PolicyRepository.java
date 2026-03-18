package org.hartford.binsure.repository;

import org.hartford.binsure.entity.Policy;
import org.hartford.binsure.enums.PolicyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long> {
    Optional<Policy> findByPolicyNumber(String policyNumber);

    List<Policy> findByBusiness_User_Id(Long userId);

    List<Policy> findByBusiness_Id(Long businessId);

    List<Policy> findByStatus(PolicyStatus status);

    @Query("SELECT p FROM Policy p WHERE p.status = :status AND p.endDate BETWEEN :startDate AND :endDate")
    List<Policy> findExpiringPolicies(@Param("status") PolicyStatus status,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}
