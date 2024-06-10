package com.madmax.campaign.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.madmax.campaign.models.Campaign;

public interface CampaignRepository extends JpaRepository<Campaign, Long> {

	@Query("from Campaign as c where c.user.userid =:userid")
	public Page<Campaign> findCampaignsByUser(@Param("userid")long userid,Pageable pageable);
}
