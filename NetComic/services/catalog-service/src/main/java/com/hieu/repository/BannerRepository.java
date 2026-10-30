package com.hieu.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.hieu.model.Banner;

@Repository
public interface BannerRepository extends JpaRepository<Banner, Long> {


    List<Banner> findByActiveTrueOrderByIdDesc();
}
