package org.hartford.binsure.service;

import org.hartford.binsure.dto.BusinessUpdateRequest;
import org.hartford.binsure.entity.Business;
import org.hartford.binsure.entity.User;
import org.hartford.binsure.exception.ResourceNotFoundException;
import org.hartford.binsure.repository.UserRepository;
import org.hartford.binsure.repository.BusinessRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class BusinessService {

    @Autowired
    private BusinessRepository businessRepository;
    @Autowired
    private UserRepository userRepository;

    public Business getBusinessById(Long businessId) {
        return businessRepository.findById(businessId)
                .orElseThrow(() -> new ResourceNotFoundException("Business", "ID", businessId));
    }

    public List<Business> getBusinessesByUserId(Long userId) {
        if (userId == null)
            return businessRepository.findAll();
        return businessRepository.findByUser_Id(userId);
    }

    public List<Business> getAllBusinesses() {
        return businessRepository.findAll();
    }

    public Business addBusinessProfile(Long userId,
            org.hartford.binsure.dto.CreateBusinessRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "ID", userId));
        Business business = new Business();
        business.setUser(user);
        business.setCompanyName(request.getCompanyName());
        business.setIndustryType(request.getIndustryType());
        business.setAnnualRevenue(request.getAnnualRevenue());
        business.setNumEmployees(request.getNumEmployees());
        business.setAddressLine1(request.getAddressLine1());
        business.setAddressLine2(request.getAddressLine2());
        business.setCity(request.getCity());
        business.setState(request.getState());
        business.setPostalCode(request.getPostalCode());
        business.setCountry(request.getCountry() != null ? request.getCountry() : "India");
        business.setCompanyRegNumber(request.getCompanyRegNumber());
        business.setTaxId(request.getTaxId());
        return businessRepository.save(business);
    }

    public Business updateBusinessProfile(Long businessId, BusinessUpdateRequest request) {
        Business business = getBusinessById(businessId);

        if (request.getCompanyName() != null)
            business.setCompanyName(request.getCompanyName());
        if (request.getIndustryType() != null)
            business.setIndustryType(request.getIndustryType());
        if (request.getAnnualRevenue() != null)
            business.setAnnualRevenue(request.getAnnualRevenue());
        if (request.getNumEmployees() != null)
            business.setNumEmployees(request.getNumEmployees());
        if (request.getAddressLine1() != null)
            business.setAddressLine1(request.getAddressLine1());
        if (request.getAddressLine2() != null)
            business.setAddressLine2(request.getAddressLine2());
        if (request.getCity() != null)
            business.setCity(request.getCity());
        if (request.getState() != null)
            business.setState(request.getState());
        if (request.getPostalCode() != null)
            business.setPostalCode(request.getPostalCode());
        if (request.getCountry() != null)
            business.setCountry(request.getCountry());

        return businessRepository.save(business);
    }

    public void deleteBusiness(Long businessId) {
        Business business = getBusinessById(businessId);
        businessRepository.delete(business);
    }
}
