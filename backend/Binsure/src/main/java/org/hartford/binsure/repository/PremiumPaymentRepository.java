package org.hartford.binsure.repository;

import org.hartford.binsure.entity.PremiumPayment;
import org.hartford.binsure.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PremiumPaymentRepository extends JpaRepository<PremiumPayment, Long> {
    Optional<PremiumPayment> findByPaymentReference(String paymentReference);

    List<PremiumPayment> findByApplication_Id(Long applicationId);

    List<PremiumPayment> findByBusiness_User_Id(Long userId);

    List<PremiumPayment> findByStatus(PaymentStatus status);

    boolean existsByApplication_IdAndStatus(Long applicationId, PaymentStatus status);
}
