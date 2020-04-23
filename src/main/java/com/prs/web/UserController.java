package com.prs.web;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import com.prs.business.JsonResponse;
import com.prs.business.User;
import com.prs.db.UserRepository;

@RestController
@RequestMapping("/users")
public class UserController {
	@Autowired
	private UserRepository userRepo;
	
	@GetMapping("/")
	public JsonResponse list() {
		JsonResponse jr = null;
		List<User> users = userRepo.findAll();
		if (users.size() > 0) {
			jr = JsonResponse.getInstance(users);
		} else {
			jr = JsonResponse.getErrorInstance("No users found.");
		}
		return jr;
	}
	
	@GetMapping("/{id}")
	public JsonResponse get(@PathVariable int id) {
		JsonResponse jr = null;
		Optional<User> actor = userRepo.findById(id);
		if (actor.isPresent()) {
			jr = JsonResponse.getInstance(actor.get());
		}
		else {
			jr = JsonResponse.getErrorInstance("No user found for ID: " + id);
		}
		return jr;
	}

	@PostMapping("/")
	public JsonResponse createUser(@RequestBody User u) {
		JsonResponse jr = null;	
		try {
			u = userRepo.save(u);
			jr = JsonResponse.getInstance(u);
		} 		
		catch (DataIntegrityViolationException dive) {
			jr = JsonResponse.getErrorInstance(dive.getRootCause().getMessage());
			dive.printStackTrace();
		}
		catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error creating user: "+e.getMessage());
			e.printStackTrace();
		}
		return jr;
	}
	
	@PutMapping("/")
	public JsonResponse updateUser(@RequestBody User u) {
		JsonResponse jr = null;	
		try {
			u = userRepo.save(u);
			jr = JsonResponse.getInstance(u);
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error updating user: "+e.getMessage());
			e.printStackTrace();
		}
		return jr;		
	}
	
	@DeleteMapping("/{id}")
	public JsonResponse deleteUser(@PathVariable int id) {
		JsonResponse jr = null;	
		try {
			userRepo.deleteById(id);
			jr = JsonResponse.getInstance("User with ID: " + id + " deleted successfully.");
		} catch (Exception e) {
			jr = JsonResponse.getErrorInstance("Error deleting user: "+e.getMessage());
			e.printStackTrace();
		}
		return jr;	
	}
	
}
