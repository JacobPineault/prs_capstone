package com.prs.web;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import com.prs.business.JsonResponse;
import com.prs.business.Request;
import com.prs.db.RequestRepository;

@RestController
@RequestMapping("/requests")
public class RequestController {

	@Autowired
	private RequestRepository requestRepo;

	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<Request> request = requestRepo.findAll();
		if (request.size() > 0) {
			jr = JsonResponse.getInstance(request);
		} else {
			jr = JsonResponse.getErrorInstance("No requests found.");
		}
		return jr;
	}

	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<Request> request = requestRepo.findById(id);
		if (request.isPresent()) {
			jr = JsonResponse.getInstance(request.get());
		} else {
			jr = JsonResponse.getErrorInstance("No request found for ID: " + id);
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createRequest(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r.setStatus("New");
			r.setSubmittedDate(LocalDate.now());
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating request: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@PutMapping("/")
	public JsonResponse updateProduct(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			r = requestRepo.save(r);
			jr = JsonResponse.getInstance(r);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating request: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@DeleteMapping("/{id}")
	public JsonResponse deleteRequest(@PathVariable int id) {
		JsonResponse jr = null;
		try {
			requestRepo.deleteById(id);
			jr = JsonResponse.getInstance("Request with ID: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting request: " + e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}

	@PutMapping("/submit-review")
	public JsonResponse submitRequestForReview(@RequestBody Request r) {
		JsonResponse jr = null;
		try {
			if (requestRepo.existsById(r.getId())) {
				if (r.getTotal() <= 50.00) {
					r.setStatus("Approved");
					r.setSubmittedDate(LocalDate.now());
				} else {
					r.setStatus("Review");
					r.setSubmittedDate(LocalDate.now());
				}
				jr = JsonResponse.getInstance(requestRepo.save(r));
			} else {
				jr = JsonResponse.getInstance(
						"PurchaseRequest ID: " + r.getId() + " does not exist and you are attempting to save it");
			}

		} catch (Exception e) {
			jr = JsonResponse.getInstance(e);
		}
		return jr;
	}

}
