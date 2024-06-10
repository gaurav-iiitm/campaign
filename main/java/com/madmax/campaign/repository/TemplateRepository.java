package com.madmax.campaign.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.madmax.campaign.models.MailTemplate;

@Repository
public interface TemplateRepository extends JpaRepository<MailTemplate, Long> {

	@Query("from MailTemplate as t where t.user.userid =:userid")
	public Page<MailTemplate> findTemplatesByUser(@Param("userid")long userid,Pageable pageable);
	
}
