package com.prs.db;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prs.business.LineItem;
import com.prs.business.Request;

public interface LineItemRepository extends JpaRepository<LineItem, Integer> {

	Iterable<LineItem> findByRequest(Request request);

}
