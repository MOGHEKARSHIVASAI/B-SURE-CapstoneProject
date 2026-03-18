package org.hartford.binsure.repository;

import org.hartford.binsure.entity.Business;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusinessRepository extends JpaRepository<Business, Long> {
    List<Business> findByUser_Id(Long userId);

    Optional<Business> findByCompanyRegNumber(String companyRegNumber);

    Optional<Business> findByTaxId(String taxId);
}
