package com.turfconnect.orgadmin.repository;

import com.turfconnect.orgadmin.model.Franchise;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FranchiseRepository extends MongoRepository<Franchise, String> {
    List<Franchise> findByOrganizationId(String organizationId);
}
