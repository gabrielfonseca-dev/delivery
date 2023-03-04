package com.algasko.delivery.data.repository

import com.algasko.delivery.data.entity.Volume
import org.springframework.data.jpa.repository.JpaSpecificationExecutor
import org.springframework.data.jpa.repository.support.JpaRepositoryImplementation
import org.springframework.stereotype.Repository

@Repository
interface VolumeRepository : JpaRepositoryImplementation<Volume, Long>, JpaSpecificationExecutor<Volume>