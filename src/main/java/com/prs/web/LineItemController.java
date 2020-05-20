package com.prs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import com.prs.business.JsonResponse;
import com.prs.business.LineItem;
import com.prs.business.Request;
import com.prs.db.LineItemRepository;
import com.prs.db.RequestRepository;

@CrossOrigin
@RestController
@RequestMapping("/line-items")
public class LineItemController {
	@Autowired
	private LineItemRepository lineItemRepo;
	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		try {
			List<LineItem> lineItem = lineItemRepo.findAll();
			if (lineItem.size() > 0) {
				jr = JsonResponse.getInstance(lineItem);
			} else {
				jr = JsonResponse.getErrorInstance("No line items found.");
			}
		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		try {
		Optional<LineItem> lineItem = lineItemRepo.findById(id);
		if (lineItem.isPresent()) {
			jr = JsonResponse.getInstance(lineItem.get());
		} else {
			jr = JsonResponse.getErrorInstance("No line item found for ID: " + id);
		}
		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			li = lineItemRepo.save(li);
			jr = JsonResponse.getInstance(li);
			calculatePurchaseRequestTotal(li);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating line item: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateLineItem(@RequestBody LineItem li) {
		JsonResponse jr = null;
		try {
			if (lineItemRepo.existsById(li.getId())) {
				jr = JsonResponse.getInstance(lineItemRepo.save(li));
				calculatePurchaseRequestTotal(li);
			} else {
				jr = JsonResponse.getInstance(
						"Line Item ID: " + li.getId() + " does not exist and you are attempting to save it");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteLineItem(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			if (lineItemRepo.existsById(id)) {
				LineItem li = lineItemRepo.findById(id).orElse(null);
				lineItemRepo.deleteById(id);
				jr = JsonResponse.getInstance(li);
				calculatePurchaseRequestTotal(li);
			} else {
				jr = JsonResponse.getInstance("Delete failed. No Lineitem found for ID: " + id);
			}
			jr = JsonResponse.getInstance("Line item with ID: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getInstance("Error deleting line item: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	// original
	@GetMapping("/lines-for-pr/{id}")
	public JsonResponse getLineItemsForPurchaseRequest(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			if (requestRepo.existsById(id)) {
				Request r = requestRepo.findById(id).orElse(null);
				jr = JsonResponse.getInstance(lineItemRepo.findAllByRequest(r));
			} else {
				jr = JsonResponse.getInstance("No request for for ID: " + id);
			}
		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
		}
		return jr;
	}

	private void calculatePurchaseRequestTotal(LineItem li) {
		double sumTotal = 0;
		Request r = li.getRequest();
		Iterable<LineItem> lis = lineItemRepo.findAllByRequest(r);
		for (LineItem rli : lis) {
			sumTotal += rli.getQuantity() * rli.getProduct().getPrice();
		}
		li.getRequest().setTotal(sumTotal);
		requestRepo.save(r);
	}
	
}
