package com.storyloom.archive.repository;

import com.storyloom.archive.model.Collection;
import com.storyloom.archive.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    
    List<Collection> findByUserOrderByNameAsc(User user);
}