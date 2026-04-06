package org.hartford.binsure.repository;

import org.hartford.binsure.entity.PolicyApplication;
import org.hartford.binsure.enums.ApplicationStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PolicyApplicationRepository extends JpaRepository<PolicyApplication, Long> {
    List<PolicyApplication> findByBusiness_User_Id(Long userId);

    List<PolicyApplication> findByStatus(ApplicationStatus status);

    List<PolicyApplication> findByAssignedUnderwriter_Id(Long underwriterId);

    List<PolicyApplication> findByAssignedUnderwriterIsNull();
}
